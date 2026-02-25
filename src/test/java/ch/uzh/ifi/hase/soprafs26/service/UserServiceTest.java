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
		testUser.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
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
