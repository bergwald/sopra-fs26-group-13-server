package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

@DataJpaTest
public class SessionUserRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SessionUserRepository sessionUserRepository;

    @Test
    public void testFindSessionUserByUserId_success() {
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setBio("Short bio");
        user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
        user.setToken("1");
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 1, 1, 8, 30, 00);
        Session session = new Session();
        session.setRoundNumber(0);
        session.setSessionExpiryDateTime(currentDateTime);
        session.setRoundStartedDateTime(currentDateTime);

        entityManager.persist(user);
        entityManager.persist(session);
        entityManager.flush();

        SessionUser sessionUser = new SessionUser();

        sessionUser.setUser(user);
        sessionUser.setSession(session);

        entityManager.persist(sessionUser);
        entityManager.flush();

        SessionUser foundSessionUser = sessionUserRepository.findById(user.getId())
                .orElseThrow(() -> new AssertionError("SessionUser not found for user id " + user.getId()));
        ;
        assertNotNull(foundSessionUser);
        assertEquals(user.getId(), foundSessionUser.getUser().getId());
        assertEquals(session.getId(), foundSessionUser.getSession().getId());
        assertEquals(0L, foundSessionUser.getScore());
        assertEquals(UserSessionRole.OWNER, foundSessionUser.getUserRole());
    }

    @Test
    public void testFindSessionUserByUserIdAndSessionId_success() {
        User user = new User();
        user.setUsername("firstname@lastname");
        user.setBio("Short bio");
        user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
        user.setToken("1");
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 1, 1, 8, 30, 00);
        Session session = new Session();
        session.setRoundNumber(0);
        session.setSessionExpiryDateTime(currentDateTime);
        session.setRoundStartedDateTime(currentDateTime);

        entityManager.persist(user);
        entityManager.persist(session);
        entityManager.flush();

        SessionUser sessionUser = new SessionUser();

        sessionUser.setUser(user);
        sessionUser.setSession(session);

        entityManager.persist(sessionUser);
        entityManager.flush();

        SessionUser foundSessionUser = sessionUserRepository.findByUserIdAndSessionId(user.getId(), session.getId())
                .orElseThrow(() -> new AssertionError("SessionUser not found for user id " + user.getId()));
        ;

        assertNotNull(foundSessionUser);
        assertEquals(user.getId(), foundSessionUser.getUser().getId());
        assertEquals(session.getId(), foundSessionUser.getSession().getId());
        assertEquals(0L, foundSessionUser.getScore());
        assertEquals(UserSessionRole.OWNER, foundSessionUser.getUserRole());

    }
}
