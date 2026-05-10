package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.SearchRegion;
import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;
import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

/*
Service class for handling everything related to a game session. 
It includes functions for creating, getting and joining sessions. 
*/

@Service
@Transactional
public class SessionService {
    private static final int EXPIRE_HOURS = 2;
    private static final int GAME_TOTAL_ROUNDS = 3;

    private final GameDataRepository gameDataRepository;
    private final SessionRepository sessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final UserRepository userRepository;
    private final GooglePanoramaService googlePanoramaService;

    public SessionService(
            GameDataRepository gameDataRepository,
            @Qualifier("sessionRepository") SessionRepository sessionRepository,
            SessionUserRepository sessionUserRepository,
            UserRepository userRepository,
            GooglePanoramaService googlePanoramaService) {
        this.gameDataRepository = gameDataRepository;
        this.sessionRepository = sessionRepository;
        this.sessionUserRepository = sessionUserRepository;
        this.userRepository = userRepository;
        this.googlePanoramaService = googlePanoramaService;
    }

    public List<Session> getAllSessions() {
        return this.sessionRepository.findAll();
    }

    public Session createNewSession(Long userId) {
        cleanUpSessionBeforeCreating(userId);
        Session session = new Session();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(EXPIRE_HOURS);
        session.setSessionExpiryDateTime(expiryDate);
        session.setRoundStartedDateTime(LocalDateTime.now());
        session.setRoundNumber(0);
        session = sessionRepository.save(session);
        sessionRepository.flush();
        return session;
    }

    public Session userJoinSession(Long userId, UUID sessionId, UserSessionRole userSessionRole)
            throws ResponseStatusException {
        Session currentSession = this.sessionRepository.findById(sessionId);
        if (currentSession == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Session id %s was not found.", sessionId.toString()));
        }
        if (currentSession.getRoundNumber() != 0) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("Session with id %s is already in progress. You can't join this session.",
                            sessionId.toString()));
        }
        cleanUpSessionBeforeCreating(userId);
        SessionUser sessionUser = sessionUserRepository.findById(userId)
                .orElse(createNewSessionUser(userId, sessionId, userSessionRole));

        this.sessionUserRepository.save(sessionUser);
        this.sessionUserRepository.flush();
        return currentSession;
    }

    public SessionUser createNewSessionUser(Long userId, UUID sessionId, UserSessionRole userSessionRole)
            throws ResponseStatusException {
        SessionUser newSessionUser = new SessionUser();
        newSessionUser.setScore(0L);
        newSessionUser.setUser(
                this.userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id %d was not found.", userId))));
        Session session = this.sessionRepository.findById(sessionId);

        if (userSessionRole == UserSessionRole.OWNER) {
            session = increaseSessionExpiryDate(session);
        }

        newSessionUser.setSession(session);
        newSessionUser.setUserRole(userSessionRole);
        return newSessionUser;
    }

    public List<SessionUser> getAllSessionUser(UUID sessionId) throws ResponseStatusException {
        List<SessionUser> sessionUser = this.sessionUserRepository.findBySessionId(sessionId);
        if (!sessionUser.isEmpty()) {
            return sessionUser;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user to session associated found!");
        }
    }

    public void resetCoordinatesOfSessionUsers(UUID sessionId) throws ResponseStatusException {
        List<SessionUser> sessionUsers = getAllSessionUser(sessionId);
        for (SessionUser su : sessionUsers) {
            su.setGuessLatitude(-1);
            su.setGuessLongitude(-1);
        }
        sessionUserRepository.saveAllAndFlush(sessionUsers);
    }

    public void resetUserGuessSubmitted(UUID sessionId) throws ResponseStatusException {
        List<SessionUser> sessionUsers = getAllSessionUser(sessionId);
        for (SessionUser su : sessionUsers) {
            su.setGuessSubmitted(false);
        }
        sessionUserRepository.saveAllAndFlush(sessionUsers);
    }

    public Session validateSessionExpiryDate(Session session) {
        if (session.getSessionExpiryDateTime().isAfter(LocalDateTime.now().plusHours(EXPIRE_HOURS))) {
            deleteSession(session);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    String.format("Session id %s is expired.", session.getIdAsString()));
        }
        return session;
    }

    public Session increaseSessionExpiryDate(Session session) {
        session.setSessionExpiryDateTime(LocalDateTime.now().plusHours(EXPIRE_HOURS));
        return session;
    }

    public boolean deleteSession(Session session) {
        /* Deletes a session with the key constraint corresponding sessionUser table. */

        try {
            List<SessionUser> sessionUsers = getAllSessionUser(session.getId());
            for (SessionUser su : sessionUsers) {
                this.sessionUserRepository.delete(su);
            }
            this.sessionUserRepository.flush();

            this.sessionRepository.delete(session);
            this.sessionRepository.flush();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to delete session: " + e.getMessage());
            return false;
        }
    }

    public List<SessionUser> getAllSessionUserForAuthorizedUser(UUID sessionId, Long userId)
            throws ResponseStatusException {
        this.sessionUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session user not found"));
        return getAllSessionUser(sessionId);
    }


    public void initializeGameDate(Session session, String searchRegion) {
        List<SearchRegion> searchRegions = googlePanoramaService.getSearchRegionsFromString(searchRegion);
        for (int roundNumber = 1; roundNumber <= GAME_TOTAL_ROUNDS; roundNumber++) {
            GooglePanoramaCandidate candidate = googlePanoramaService.fetchPanoramaCandidate(searchRegions);

            Game_data gameData = new Game_data();
            gameData.setSessionId(session.getIdAsString());
            gameData.setRoundNumber(roundNumber);
            gameData.setImageUrl(candidate.panoId());
            gameData.setLatitude(candidate.latitude());
            gameData.setLongitude(candidate.longitude());
            gameDataRepository.save(gameData);
        }
        gameDataRepository.flush();
    }

    public boolean checkIfUserIsSessionOwner(Long userId, String sessionId) {
        UUID sessionUuid = parseSessionId(sessionId);
        Optional<SessionUser> sessionUser = sessionUserRepository.findByUserIdAndSessionId(userId, sessionUuid);
        if (sessionUser.isPresent()) {
            if (sessionUser.get().getUserRole().equals(UserSessionRole.OWNER)) {
                return true;
            }
        }
        return false;
    }

    public Session getSessionWithId(String sessionId) {
        try {
            UUID sessionUuid = parseSessionId(sessionId);
            Session session = sessionRepository.findById(sessionUuid);
            return session;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
    }

    public int increaseSessionRoundNumber(Session session, int submittedRoundNumber, int totalRoundNumbers) {
        int nextRoundNumber = submittedRoundNumber < totalRoundNumbers
                ? submittedRoundNumber + 1
                : totalRoundNumbers + 1;
        session.setRoundNumber(nextRoundNumber);
        session.setRoundStartedDateTime(LocalDateTime.now());
        sessionRepository.save(session);
        return nextRoundNumber;
    }

    public void validateSessionGameGuess(String sessionId, int submittedRoundNumber) throws ResponseStatusException {
        UUID sessionUuid = parseSessionId(sessionId);
        Session session = sessionRepository.findById(sessionUuid);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
        if (session.getRoundNumber() != submittedRoundNumber) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session round is out of sync");
        }
        if (submittedRoundNumber < 1 || submittedRoundNumber > GAME_TOTAL_ROUNDS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid round number");
        }
    }

    private static UUID parseSessionId(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid session id");
        }
    }

    public void cleanUpSessionBeforeCreating(Long userId) {
        Optional<SessionUser> sessionUser = sessionUserRepository.findById(userId);
        if (sessionUser.isPresent()) {
            if (sessionUser.get().getUserRole() == UserSessionRole.OWNER) {
                deleteSession(sessionUser.get().getSession());
            } else {
                this.sessionUserRepository.delete(sessionUser.get());
                this.sessionUserRepository.flush();
            }
        }
    }

}
