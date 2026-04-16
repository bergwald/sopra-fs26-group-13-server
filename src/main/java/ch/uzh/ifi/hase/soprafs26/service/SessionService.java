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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@Transactional
public class SessionService {
    private static final int EXPIRE_HOURS = 2;

    private final Logger log = LoggerFactory.getLogger(SessionService.class);

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
        log.debug("Create session with id: " + session.getIdAsString());
        return session;
    }

    public Session userJoinSession(Long userId, UUID sessionId, UserSessionRole UserSessionRole) {

        Session currentSession = this.sessionRepository.findById(sessionId);
        SessionUser newSessionUser = new SessionUser();
        newSessionUser.setScore(0L);
        newSessionUser.setUser(
                this.userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("User with id %d was not found.", userId))));

        newSessionUser.setSession(this.sessionRepository.findById(sessionId));
        newSessionUser.setUserRole(UserSessionRole);
        this.sessionUserRepository.save(newSessionUser);
        this.sessionUserRepository.flush();
        return currentSession;
    }

    public List<SessionUser> getAllSessionUser(UUID sessionId) {
        List<SessionUser> sessionUser = this.sessionUserRepository.findBySessionId(sessionId);
        if (!sessionUser.isEmpty()) {
            return sessionUserRepository.findBySessionId(sessionId);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user to session associated found!");
        }
    }
}
