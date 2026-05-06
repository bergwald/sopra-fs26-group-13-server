package ch.uzh.ifi.hase.soprafs26.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.User;

@DataJpaTest
public class UserRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void findByUsername_success() {
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setBio("Short bio");
		user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
		user.setToken("1");

		entityManager.persist(user);
		entityManager.flush();

		User found = userRepository.findByUsername(user.getUsername());

		assertNotNull(found.getId());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getBio(), user.getBio());
		assertEquals(found.getToken(), user.getToken());
	}

	@Test
	public void findByToken_success() {
		User user = new User();
		user.setUsername("firstname@lastname");
		user.setBio("Short bio");
		user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
		user.setToken("token-123");

		entityManager.persist(user);
		entityManager.flush();

		User found = userRepository.findByToken(user.getToken());

		assertNotNull(found.getId());
		assertEquals(found.getUsername(), user.getUsername());
		assertEquals(found.getBio(), user.getBio());
		assertEquals(found.getToken(), user.getToken());
	}
}
