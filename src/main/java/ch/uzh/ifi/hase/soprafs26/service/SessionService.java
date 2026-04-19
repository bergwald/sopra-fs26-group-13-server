package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
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

    private final SessionRepository sessionRepository;
    private final SessionUserRepository sessionUserRepository;
    private final UserRepository userRepository;

    public SessionService(
            @Qualifier("sessionRepository") SessionRepository sessionRepository,
            SessionUserRepository sessionUserRepository,
            UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionUserRepository = sessionUserRepository;
        this.userRepository = userRepository;
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

    public Session userJoinSession(Long userId, UUID sessionId, UserSessionRole UserSessionRole)
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
        SessionUser sessionUser = sessionUserRepository.findById(userId)
                .orElse(createNewSessionUser(userId, sessionId, UserSessionRole));

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
            return sessionUserRepository.findBySessionId(sessionId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user to session associated found!");
        }
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

    public void deleteSession(Session session) {
        /* Deletes a session with the key constraint corresponding sessionUser table. */
        List<SessionUser> sessionUsers = getAllSessionUser(session.getId());
        for (SessionUser su : sessionUsers) {
            this.sessionUserRepository.delete(su);
        }
        this.sessionUserRepository.flush();
        this.sessionRepository.delete(session);
        this.sessionRepository.flush();
    }

}
