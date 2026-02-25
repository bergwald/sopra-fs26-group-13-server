package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

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
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		String rawPassword = "password123";

		// when
		User createdUser = userService.createUser(testUser, rawPassword);

		// then
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getBio(), createdUser.getBio());
		assertNotNull(createdUser.getPasswordHash());
		assertNotEquals(rawPassword, createdUser.getPasswordHash());
		assertTrue(BCrypt.checkpw(rawPassword, createdUser.getPasswordHash()));
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertNotNull(createdUser.getCreationDate());
	}

	@Test
	public void getUserById_validId_success() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "password123");

		User foundUser = userService.getUserById(createdUser.getId());
		assertEquals(createdUser.getId(), foundUser.getId());
		assertEquals(createdUser.getName(), foundUser.getName());
		assertEquals(createdUser.getUsername(), foundUser.getUsername());
		assertEquals(createdUser.getBio(), foundUser.getBio());
		assertEquals(createdUser.getStatus(), foundUser.getStatus());
		assertNotNull(foundUser.getCreationDate());
		assertEquals(createdUser.getCreationDate(), foundUser.getCreationDate());
	}

	@Test
	public void loginUser_validCredentials_success() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		userService.createUser(testUser, "password123");

		User loggedInUser = userService.loginUser("testUsername", "password123");
		assertEquals("testUsername", loggedInUser.getUsername());
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
	}

	@Test
	public void loginUser_invalidCredentials_throwsUnauthorized() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		userService.createUser(testUser, "password123");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser("testUsername", "wrongPassword"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void logoutUser_validToken_success() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		User createdUser = userService.createUser(testUser, "password123");
		String oldToken = createdUser.getToken();

		userService.logoutUser(oldToken);

		User updatedUser = userRepository.findById(createdUser.getId()).orElseThrow();
		assertEquals(UserStatus.OFFLINE, updatedUser.getStatus());
		assertNotEquals(oldToken, updatedUser.getToken());
		assertNull(userRepository.findByToken(oldToken));
	}

	@Test
	public void logoutUser_invalidToken_throwsUnauthorized() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.logoutUser("invalid-token"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void getUserById_userDoesNotExist_throwsNotFoundException() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(999L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("User with id 999 was not found.", exception.getReason());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		userService.createUser(testUser, "password123");

		// attempt to create second user with same username
		User testUser2 = new User();
		testUser2.setName("Another Name");
		testUser2.setUsername("testUsername");
		testUser2.setBio("Another bio");

		// check that an error is thrown
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser2, "password123"));
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	public void createUser_shortPassword_throwsException() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "short"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_blankName_throwsException() {
		User testUser = new User();
		testUser.setName(" ");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_nullBio_defaultsToEmptyString() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		User createdUser = userService.createUser(testUser, "password123");
		assertEquals("", createdUser.getBio());
	}

	@Test
	public void createUser_tooLongBio_throwsException() {
		User testUser = new User();
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("a".repeat(281));
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}
}
