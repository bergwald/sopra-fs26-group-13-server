package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserUpdatePutDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	private final ControllerTestHelper controllerTestHelper = new ControllerTestHelper();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	private User sampleUser() {
		// given
		User user = new User();
		user.setId(1L);
		user.setUsername("testUsername");
		user.setBio("Short bio");
		user.setMascotId(3);
		user.setRoundsPlayed(4);
		user.setAvgDistance(1250.5);
		user.setScore(289L);
		user.setToken("valid-token");
		user.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));
		return user;
	}

	/**
	 * Tests GET /users and verifies it returns all users as a JSON array (200
	 * status).
	 * GIVEN
	 */
	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		List<User> allUsers = Collections.singletonList(sampleUser());
		given(userService.getUsers()).willReturn(allUsers);

		// when/then
		mockMvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(1)))
				.andExpect(jsonPath("$[0].username", is("testUsername")))
				.andExpect(jsonPath("$[0].bio", is("Short bio")))
				.andExpect(jsonPath("$[0].mascot_id", is(3)))
				.andExpect(jsonPath("$[0].rounds_played", is(4)))
				.andExpect(jsonPath("$[0].avg_distance", is(1250.5)))
				.andExpect(jsonPath("$[0].score", is(289)));
	}

	/**
	 * Tests POST /users and verifies a valid request creates and returns a user
	 * (201 status).
	 * GIVEN
	 */
	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = sampleUser();
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("Short bio");

		given(userService.createUser(any(), anyString())).willReturn(user);

		// when/then -> do the request + validate the result
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(controllerTestHelper.asJsonString(userPostDTO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.bio", is("Short bio")))
				.andExpect(jsonPath("$.mascot_id", is(3)))
				.andExpect(jsonPath("$.token", is("valid-token")));
	}

	/**
	 * Tests GET /users/{userId} and verifies an existing user is returned as JSON
	 * (200 status).
	 */
	@Test
	public void givenUserId_whenGetUser_thenReturnJsonObject() throws Exception {
		// given
		User user = sampleUser();
		given(userService.getUserById(1L)).willReturn(user);

		// when/then
		mockMvc.perform(get("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.bio", is("Short bio")))
				.andExpect(jsonPath("$.mascot_id", is(3)))
				.andExpect(jsonPath("$.rounds_played", is(4)))
				.andExpect(jsonPath("$.avg_distance", is(1250.5)))
				.andExpect(jsonPath("$.score", is(289)))
				.andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())));

		verify(userService, never()).extractBearerToken(anyString());
		verify(userService, never()).getAuthenticatedUser(anyString());
	}

	/** Tests GET /users/{userId} and verifies a missing Authorization header still returns the public profile. */
	@Test
	public void givenMissingAuthorization_whenGetUser_thenReturnJsonObject() throws Exception {
		// given
		User user = sampleUser();
		given(userService.getUserById(1L)).willReturn(user);

		// when/then
		mockMvc.perform(get("/users/{userId}", 1L).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.bio", is("Short bio")))
				.andExpect(jsonPath("$.mascot_id", is(3)))
				.andExpect(jsonPath("$.rounds_played", is(4)))
				.andExpect(jsonPath("$.avg_distance", is(1250.5)))
				.andExpect(jsonPath("$.score", is(289)));

		verify(userService, never()).extractBearerToken(anyString());
		verify(userService, never()).getAuthenticatedUser(anyString());
	}

	/**
	 * Tests POST /login and verifies valid credentials return the logged-in user
	 * data (200 status).
	 */
	@Test
	public void loginUser_validInput_userLoggedIn() throws Exception {
		// given
		User user = sampleUser();
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("password123");

		given(userService.loginUser("testUsername", "password123")).willReturn(user);

		// when/then
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(controllerTestHelper.asJsonString(userLoginDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.username", is("testUsername")))
				.andExpect(jsonPath("$.bio", is("Short bio")))
				.andExpect(jsonPath("$.mascot_id", is(3)))
				.andExpect(jsonPath("$.token", is("valid-token")));
	}

	/**
	 * Tests POST /login and verifies invalid credentials return 401 Unauthorized.
	 */
	@Test
	public void loginUser_invalidCredentials_unauthorized() throws Exception {
		// given
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("wrongPassword");

		given(userService.loginUser("testUsername", "wrongPassword"))
				.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"The username or password provided is incorrect."));

		// when/then
		mockMvc.perform(post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(controllerTestHelper.asJsonString(userLoginDTO)))
				.andExpect(status().isUnauthorized());
	}

	/**
	 * Tests POST /logout and verifies a valid bearer token logs out with 204 No
	 * Content.
	 */
	@Test
	public void logoutUser_validToken_noContent() throws Exception {
		// given
		when(userService.extractBearerToken(anyString())).thenReturn("valid-token");

		// when/then
		mockMvc.perform(post("/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token"))
				.andExpect(status().isNoContent());

		verify(userService).logoutUser("valid-token");
	}

	/**
	 * Tests POST /logout and verifies a missing Authorization header returns 401.
	 */
	@Test
	public void logoutUser_missingAuthorizationHeader_unauthorized() throws Exception {
		// given
		when(userService.extractBearerToken(null))
				.thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));

		// when/then
		mockMvc.perform(post("/logout").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	/**
	 * Tests PUT /users/{userId} and verifies a valid bio-only update returns 204.
	 */
	@Test
	public void updateUser_bioOnly_noContent() throws Exception {
		// given
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		when(userService.extractBearerToken(anyString())).thenReturn("valid-token");

		// when/then
		mockMvc.perform(put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(controllerTestHelper.asJsonString(userUpdatePutDTO)))
				.andExpect(status().isNoContent());

		verify(userService).updateUser(1L, "valid-token", "Updated bio", null, null);
	}

	@Test
	public void updateUser_mascotOnly_noContent() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setMascot_id(7);

		when(userService.extractBearerToken(anyString())).thenReturn("valid-token");

		mockMvc.perform(put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(controllerTestHelper.asJsonString(userUpdatePutDTO)))
				.andExpect(status().isNoContent());

		verify(userService).updateUser(1L, "valid-token", null, null, 7);
	}

	/** Tests PUT /users/{userId} and verifies an invalid token returns 401. */
	@Test
	public void updateUser_invalidToken_unauthorized() throws Exception {
		// given
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		when(userService.extractBearerToken(anyString()))
				.thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));

		// when/then
		mockMvc.perform(put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer invalid-token")
				.content(controllerTestHelper.asJsonString(userUpdatePutDTO)))
				.andExpect(status().isUnauthorized());
	}

	/** Tests POST /users and verifies a duplicate username returns 409 Conflict. */
	@Test
	public void createUser_duplicateUsername_conflict() throws Exception {
		// given
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("Short bio");

		given(userService.createUser(any(), anyString()))
				.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "The username provided is not unique."));

		// when/then
		mockMvc.perform(post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(controllerTestHelper.asJsonString(userPostDTO)))
				.andExpect(status().isConflict());
	}
}
