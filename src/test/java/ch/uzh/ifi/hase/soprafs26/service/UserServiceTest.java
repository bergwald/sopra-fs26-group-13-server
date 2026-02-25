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
		assertNotNull(createdUser.getPasswordHash());
		assertNotEquals(rawPassword, createdUser.getPasswordHash());
		assertTrue(BCrypt.checkpw(rawPassword, createdUser.getPasswordHash()));
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
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

}
