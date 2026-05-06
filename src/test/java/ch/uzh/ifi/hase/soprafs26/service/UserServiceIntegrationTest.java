package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
		userRepository.deleteAll();
	}

	@Test
	public void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");

		// when
		User createdUser = userService.createUser(testUser, "password123");

		// then
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getBio(), createdUser.getBio());
		assertNotNull(createdUser.getPasswordHash());
		assertTrue(BCrypt.checkpw("password123", createdUser.getPasswordHash()));
		assertNotNull(createdUser.getToken());
		assertNotNull(createdUser.getCreationDate());
	}

	@Test
	public void getUserById_validId_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "password123");

		// when
		User foundUser = userService.getUserById(createdUser.getId());

		// then
		assertEquals(createdUser.getId(), foundUser.getId());
		assertEquals(createdUser.getUsername(), foundUser.getUsername());
		assertEquals(createdUser.getBio(), foundUser.getBio());
		assertEquals(createdUser.getCreationDate(), foundUser.getCreationDate());
	}

	@Test
	public void getAuthenticatedUser_validToken_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "password123");

		// when
		User authenticatedUser = userService.getAuthenticatedUser(createdUser.getToken());

		// then
		assertEquals(createdUser.getId(), authenticatedUser.getId());
		assertEquals(createdUser.getUsername(), authenticatedUser.getUsername());
	}

	@Test
	public void loginUser_validCredentials_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		userService.createUser(testUser, "password123");

		// when
		User loggedInUser = userService.loginUser("testUsername", "password123");

		// then
		assertEquals("testUsername", loggedInUser.getUsername());
		assertNotNull(loggedInUser.getToken());
	}

	@Test
	public void logoutUser_validToken_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "password123");
		String oldToken = createdUser.getToken();

		// when
		userService.logoutUser(oldToken);

		// then
		User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
		assertNotEquals(oldToken, updatedUser.getToken());
		assertNull(userRepository.findByToken(oldToken));
	}

	@Test
	public void updateUser_passwordOnly_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "oldPassword123");
		String oldToken = createdUser.getToken();

		// when
		userService.updateUser(createdUser.getId(), oldToken, null, "newPassword123");

		// then
		User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
		assertNotEquals(oldToken, updatedUser.getToken());
		assertTrue(BCrypt.checkpw("newPassword123", updatedUser.getPasswordHash()));
	}

	@Test
	public void updateUser_bioOnly_success() {
		// given
		User testUser = new User();
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "oldPassword123");

		// when
		userService.updateUser(createdUser.getId(), createdUser.getToken(), "  Updated bio  ", null);

		// then
		User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
		assertEquals("Updated bio", updatedUser.getBio());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		// given
		User firstUser = new User();
		firstUser.setUsername("testUsername");
		firstUser.setBio("Short bio");
		userService.createUser(firstUser, "password123");

		User secondUser = new User();
		secondUser.setUsername("testUsername");
		secondUser.setBio("Another bio");

		// when
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(secondUser, "password123"));

		// then
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	public void createUser_blankUsername_throwsException() {
		// given
		User testUser = new User();
		testUser.setUsername(" ");
		testUser.setBio("Short bio");

		// when
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));

		// then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}
}
