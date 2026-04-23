package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

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
		testUser.setUsername("testUsername");
		testUser.setBio("Short bio");
		testUser.setToken("valid-token");
		testUser.setStatus(UserStatus.OFFLINE);
		testUser.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		// when -> any object being saved in the userRepository returns the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(null);
	}

	@Test
	public void getAuthenticatedUser_tokenNull(){
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getAuthenticatedUser(null));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void getAuthenticatedTargetUser_WrongBio(){
		User newUser = new User();
		newUser.setId(2L);
		newUser.setToken("Token2");
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
		Mockito.when(userRepository.findByToken("Token")).thenReturn(testUser);
		Mockito.when(userService.getAuthenticatedUser("Token")).thenReturn(testUser);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getAuthorizedTargetUser(2L, "Token"));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void getAuthenticatedTargetUser_targetUserNotFound(){
		Mockito.when(userRepository.findById(2L)).thenReturn(Optional.empty());
		Mockito.when(userRepository.findByToken("Token")).thenReturn(testUser);
		Mockito.when(userService.getAuthenticatedUser("Token")).thenReturn(testUser);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getAuthorizedTargetUser(2L, "Token"));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	public void createUser_validInputs_success() {
		// given
		String rawPassword = "password123";

		// when
		User createdUser = userService.createUser(testUser, rawPassword);

		// then
		Mockito.verify(userRepository).save(Mockito.any());
		Mockito.verify(userRepository).flush();
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getBio(), createdUser.getBio());
		assertNotNull(createdUser.getPasswordHash());
		assertTrue(BCrypt.checkpw(rawPassword, createdUser.getPasswordHash()));
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}

	@Test
	public void createUser_blankUsername_throwsException() {
		// given
		testUser.setUsername("                    ");

		// when
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));

		// then
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_shortPassword_throwsException() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "short"));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}	

	@Test
	public void createUser_nullPassword_throwsException() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, null));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void createUser_duplicateUsername_throwsException() {
		// given
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

		// when
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.createUser(testUser, "password123"));

		// then
		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	@Test
	public void createUser_bioTooLong_throwsException(){
		testUser.setBio("Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat mass");
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
	public void loginUser_validCredentials_success() {
		// given
		String rawPassword = "password123";
		testUser.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);

		// when
		User loggedInUser = userService.loginUser("testUsername", rawPassword);

		// then
		Mockito.verify(userRepository).save(testUser);
		Mockito.verify(userRepository).flush();
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
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
	public void loginUser_noPassword_throwsUnauthorized() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.loginUser("testUsername", ""));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void logoutUser_validToken_success() {
		// given
		String oldToken = "valid-token";
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);

		// when
		userService.logoutUser(oldToken);

		// then
		Mockito.verify(userRepository).save(testUser);
		Mockito.verify(userRepository).flush();
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
		assertNotEquals(oldToken, testUser.getToken());
	}

	@Test
	public void logoutUsers_nullToken(){
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.logoutUser(null));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void logoutUsers_UserNotFound(){
		Mockito.when(userRepository.findByToken("token")).thenReturn(null);
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.logoutUser("token"));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}
	@Test
	public void updateUser_bioOnly_success() {
		// given
		String oldToken = "valid-token";
		String oldPasswordHash = BCrypt.hashpw("oldPassword123", BCrypt.gensalt());
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		testUser.setPasswordHash(oldPasswordHash);
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		// when
		userService.updateUser(1L, oldToken, "  Updated bio  ", null);

		// then
		assertEquals("Updated bio", testUser.getBio());
		assertEquals(UserStatus.ONLINE, testUser.getStatus());
		assertEquals(oldToken, testUser.getToken());
		assertEquals(oldPasswordHash, testUser.getPasswordHash());
	}

	@Test
	public void updateUser_passwordOnly_success() {
		// given
		String oldToken = "valid-token";
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		testUser.setPasswordHash(BCrypt.hashpw("oldPassword123", BCrypt.gensalt()));
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		// when
		userService.updateUser(1L, oldToken, null, "newPassword123");

		// then
		assertEquals(UserStatus.OFFLINE, testUser.getStatus());
		assertNotEquals(oldToken, testUser.getToken());
		assertTrue(BCrypt.checkpw("newPassword123", testUser.getPasswordHash()));
	}

	@Test
	public void updateUser_passwordEmpty(){
		String oldToken = "valid-token";
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		testUser.setPasswordHash(BCrypt.hashpw("oldPassword123", BCrypt.gensalt()));
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
			() -> userService.updateUser(1L, oldToken, null, ""));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void updateUser_passwordTooShort(){
		String oldToken = "valid-token";
		testUser.setStatus(UserStatus.ONLINE);
		testUser.setToken(oldToken);
		testUser.setPasswordHash(BCrypt.hashpw("oldPassword123", BCrypt.gensalt()));
		Mockito.when(userRepository.findByToken(oldToken)).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
			() -> userService.updateUser(1L, oldToken, null, "short"));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void updateUser_noFields_throwsBadRequest() {
		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.updateUser(1L, "valid-token", null, null));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	@Test
	public void getUserById_validId_success() {
		// given
		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

		// when
		User foundUser = userService.getUserById(1L);

		// then
		assertEquals(testUser.getId(), foundUser.getId());
		assertEquals(testUser.getUsername(), foundUser.getUsername());
		assertEquals(testUser.getBio(), foundUser.getBio());
		assertEquals(testUser.getCreationDate(), foundUser.getCreationDate());
	}

	@Test
	public void getAuthenticatedUser_validToken_success() {
		// given
		Mockito.when(userRepository.findByToken("valid-token")).thenReturn(testUser);

		// when
		User authenticatedUser = userService.getAuthenticatedUser("valid-token");

		// then
		assertEquals(testUser.getId(), authenticatedUser.getId());
		assertEquals(testUser.getUsername(), authenticatedUser.getUsername());
	}

	@Test
	public void getAuthenticatedUser_invalidToken_throwsUnauthorized() {
		// when
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getAuthenticatedUser("invalid-token"));

		// then
		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	@Test
	public void getUsers_returnsList(){
		Mockito.when(userRepository.findAll()).thenReturn(null);
		assertEquals(null, userService.getUsers());

	}

	@Test
	public void gitUserById_ExceptionNotFound(){
		Mockito.when(userRepository.findById(0L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.getUserById(0L));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	public void extractBearerToken_null(){
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.extractBearerToken(null));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}	
	
	@Test
	public void extractBearerToken_empty(){
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> userService.extractBearerToken("Bearer    "));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}
	@Test
	public void extractBearerToken_valid(){
		assertEquals("aaa", userService.extractBearerToken("Bearer aaa"));
	}
}
