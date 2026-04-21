package ch.uzh.ifi.hase.soprafs26.service;

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
    private static final int SINGLEPLAYER_TOTAL_ROUNDS = 3;

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

    public Session createNewSession() {
        Session session = new Session();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(EXPIRE_HOURS);
        session.setSessionExpiryDateTime(expiryDate);
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

        SessionUser newSessionUser = new SessionUser();
        newSessionUser.setScore(0L);
        newSessionUser.setUser(
                this.userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id %d was not found.", userId))));

        newSessionUser.setSession(this.sessionRepository.findById(sessionId));
        newSessionUser.setUserRole(userSessionRole);
        if (userSessionRole.equals(UserSessionRole.OWNER)){
            // Initializes the game if the user is the owner
            initializeGameDate(currentSession);
        }
        this.sessionUserRepository.save(newSessionUser);
        this.sessionUserRepository.flush();
        return currentSession;
    }

    public List<SessionUser> getAllSessionUser(UUID sessionId) throws ResponseStatusException {
        List<SessionUser> sessionUser = this.sessionUserRepository.findBySessionId(sessionId);
        if (!sessionUser.isEmpty()) {
            return sessionUser;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user to session associated found!");
        }
    }

    public List<SessionUser> getAllSessionUserForAuthorizedUser(UUID sessionId, Long userId)
            throws ResponseStatusException {
        this.sessionUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session user not found"));
        return getAllSessionUser(sessionId);
    }   

    private void initializeGameDate(Session session) {
        for (int roundNumber = 1; roundNumber <= SINGLEPLAYER_TOTAL_ROUNDS; roundNumber++) {
            GooglePanoramaCandidate candidate = googlePanoramaService.fetchPanoramaCandidate();

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
    
}
