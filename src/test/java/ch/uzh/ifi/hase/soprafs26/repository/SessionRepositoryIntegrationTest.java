package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;


@DataJpaTest
public class SessionRepositoryIntegrationTest {
    
    @Autowired
	private TestEntityManager entityManager;


    @Autowired
    private SessionRepository sessionRepository;

    @Test
    public void findById_success(){

        LocalDateTime currentDateTime = LocalDateTime.of(2026, 1, 1, 8, 30, 00);
        Session session = new Session();
        session.setRoundNumber(0);
        session.setSessionExpiryDateTime(currentDateTime);

        entityManager.persist(session);
        entityManager.flush();
        
        UUID createdUUID = session.getId();

        Session foundSession = sessionRepository.findById(createdUUID);
        assertNotNull(foundSession.getId());
        assertEquals(foundSession.getRoundNumber(), session.getRoundNumber());
        assertEquals(foundSession.getSessionExpiryDateTime(), currentDateTime);
    }
}
