package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setName("Test User");
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		testUser.setToken("valid-token");
		testUser.setStatus(UserStatus.OFFLINE);
		testUser.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(null);
	}

	@Test
	public void createUser_validInputs_success() {
		String rawPassword = "password123";
		User createdUser = userService.createUser(testUser, rawPassword);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

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
	public void loginUser_validCredentials_success() {
		String rawPassword = "password123";
		testUser.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

		User loggedInUser = userService.loginUser("testUsername", rawPassword);

		Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
		Mockito.verify(userRepository, Mockito.times(1)).flush();
		assertEquals(testUser.getId(), loggedInUser.getId());
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
	}

	@Test
	public void loginUser_userNotFound_throwsUnauthorized() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser("unknownUser", "password123"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void loginUser_wrongPassword_throwsUnauthorized() {
		testUser.setPasswordHash(BCrypt.hashpw("password123", BCrypt.gensalt()));
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser("testUsername", "wrongPassword"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void logoutUser_validToken_success() {
		String oldToken = "valid-token";
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);

		userService.logoutUser(oldToken);

		Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
		Mockito.verify(userRepository, Mockito.times(1)).flush();
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
		assertNotNull(testUser.getToken());
		assertNotEquals(oldToken, testUser.getToken());
	}

	@Test
	public void logoutUser_invalidToken_throwsUnauthorized() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.logoutUser("invalid-token"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void changePassword_validRequest_success() {
		String oldToken = "valid-token";
		String newPassword = "newPassword123";
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		testUser.setPasswordHash(BCrypt.hashpw("oldPassword123", BCrypt.gensalt()));

		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		userService.changePassword(1L, oldToken, newPassword);

		Mockito.verify(userRepository, Mockito.times(1)).save(testUser);
		Mockito.verify(userRepository, Mockito.times(1)).flush();
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
		assertNotEquals(oldToken, testUser.getToken());
		assertTrue(BCrypt.checkpw(newPassword, testUser.getPasswordHash()));
	}

	@Test
	public void changePassword_invalidToken_throwsUnauthorized() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(1L, "invalid-token", "newPassword123"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
		Mockito.verify(userRepository, Mockito.never()).findById(Mockito.anyLong());
	}

	@Test
	public void changePassword_targetUserNotFound_throwsNotFound() {
		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
		Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(999L, "valid-token", "newPassword123"));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	public void changePassword_tokenUserMismatch_throwsUnauthorized() {
		User otherUser = new User();
		otherUser.setId(2L);
		otherUser.setUsername("otherUser");
		otherUser.setPasswordHash(BCrypt.hashpw("oldPassword123", BCrypt.gensalt()));
		otherUser.setToken("other-token");
		otherUser.setStatus(UserStatus.ONLINE);
		otherUser.setBio("bio");
		otherUser.setName("Other User");

		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(2L, "valid-token", "newPassword123"));
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void changePassword_shortPassword_throwsBadRequest() {
		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.changePassword(1L, "valid-token", "short"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void getUserById_validId_success() {
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		User foundUser = userService.getUserById(1L);

		Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
		assertEquals(testUser.getId(), foundUser.getId());
		assertEquals(testUser.getName(), foundUser.getName());
		assertEquals(testUser.getUsername(), foundUser.getUsername());
		assertEquals(testUser.getBio(), foundUser.getBio());
		assertEquals(testUser.getCreationDate(), foundUser.getCreationDate());
	}

	@Test
	public void getUserById_userDoesNotExist_throwsNotFoundException() {
		Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(999L));
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
		assertEquals("User with id 999 was not found.", exception.getReason());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	public void createUser_shortPassword_throwsException() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "short"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_blankUsername_throwsException() {
		testUser.setUsername(" ");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_blankName_throwsException() {
		testUser.setName(" ");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_nullBio_defaultsToEmptyString() {
		testUser.setBio(null);
		User createdUser = userService.createUser(testUser, "password123");
		assertEquals("", createdUser.getBio());
	}

	@Test
	public void createUser_whitespaceBio_defaultsToEmptyString() {
		testUser.setBio("   ");
		User createdUser = userService.createUser(testUser, "password123");
		assertEquals("", createdUser.getBio());
	}

	@Test
	public void createUser_tooLongBio_throwsException() {
		testUser.setBio("a".repeat(281));
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_exactMaxBioLength_success() {
		testUser.setBio("a".repeat(280));
		User createdUser = userService.createUser(testUser, "password123");
		assertEquals(280, createdUser.getBio().length());
	}

}
