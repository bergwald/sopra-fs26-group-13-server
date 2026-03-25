package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserUpdatePutDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	/**
	 * Tests GET /users and verifies it returns all users as a JSON array (200 status).
	 * GIVEN
	*/
	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setBio("Short bio");
		user.setStatus(UserStatus.OFFLINE);
		user.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);

		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].bio", is(user.getBio())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	/**
	 * Tests POST /users and verifies a valid request creates and returns a user (201 status).
	 * GIVEN
	 * */
	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setBio("I love coding.");
		user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("I love coding.");

		given(userService.createUser(Mockito.any(), Mockito.anyString())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
	}

	/** Tests GET /users/{userId} and verifies an existing user is returned as JSON (200 status). */
	@Test
	public void givenUserId_whenGetUser_thenReturnJsonObject() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setBio("Short bio");
		user.setStatus(UserStatus.OFFLINE);
		user.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		given(userService.getUserById(1L)).willReturn(user);

		MockHttpServletRequestBuilder getRequest = get("/users/{userId}", 1L).contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())));
	}

	/** Tests GET /users/{userId} and verifies an unknown user ID returns 404. */
	@Test
	public void givenInvalidUserId_whenGetUser_thenReturnNotFound() throws Exception {
		given(userService.getUserById(999L))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id 999 was not found."));

		MockHttpServletRequestBuilder getRequest = get("/users/{userId}", 999L).contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest).andExpect(status().isNotFound());
	}

	/** Tests POST /login and verifies valid credentials return the logged-in user data (200 status). */
	@Test
	public void loginUser_validInput_userLoggedIn() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setBio("I love coding.");
		user.setToken("valid-token");
		user.setStatus(UserStatus.ONLINE);

		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("password123");

		given(userService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword())).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userLoginDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
	}

	/** Tests POST /login and verifies invalid credentials return 401 Unauthorized. */
	@Test
	public void loginUser_invalidCredentials_unauthorized() throws Exception {
		UserLoginDTO userLoginDTO = new UserLoginDTO();
		userLoginDTO.setUsername("testUsername");
		userLoginDTO.setPassword("wrongPassword");

		given(userService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword()))
				.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username or password provided is incorrect."));

		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userLoginDTO));

		mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
	}

	/** Tests POST /logout and verifies a valid bearer token logs out with 204 No Content. */
	@Test
	public void logoutUser_validToken_noContent() throws Exception {
		MockHttpServletRequestBuilder postRequest = post("/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token");

		mockMvc.perform(postRequest).andExpect(status().isNoContent());
		Mockito.verify(userService).logoutUser("valid-token");
	}

	/** Tests POST /logout and verifies a missing Authorization header returns 401. */
	@Test
	public void logoutUser_missingAuthorizationHeader_unauthorized() throws Exception {
		MockHttpServletRequestBuilder postRequest = post("/logout").contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
	}

	/** Tests POST /logout and verifies a malformed Authorization header returns 401. */
	@Test
	public void logoutUser_malformedAuthorizationHeader_unauthorized() throws Exception {
		MockHttpServletRequestBuilder postRequest = post("/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "invalid-token");

		mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
	}

	/** Tests POST /logout and verifies an unknown bearer token returns 401. */
	@Test
	public void logoutUser_unknownToken_unauthorized() throws Exception {
		willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."))
				.given(userService).logoutUser("unknown-token");

		MockHttpServletRequestBuilder postRequest = post("/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer unknown-token");

		mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
	}

	/** Tests PUT /users/{userId} and verifies a valid bio-only update returns 204. */
	@Test
	public void updateUser_bioOnly_noContent() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isNoContent());
		Mockito.verify(userService).updateUser(1L, "valid-token", "Updated bio", null);
	}

	/** Tests PUT /users/{userId} and verifies a valid combined update returns 204. */
	@Test
	public void updateUser_bioAndPassword_noContent() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");
		userUpdatePutDTO.setNewPassword("newPassword123");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isNoContent());
		Mockito.verify(userService).updateUser(1L, "valid-token", "Updated bio", "newPassword123");
	}

	/** Tests PUT /users/{userId} and verifies a missing Authorization header returns 401. */
	@Test
	public void updateUser_missingAuthorizationHeader_unauthorized() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
	}

	/** Tests PUT /users/{userId} and verifies a malformed Authorization header returns 401. */
	@Test
	public void updateUser_malformedAuthorizationHeader_unauthorized() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "invalid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
	}

	/** Tests PUT /users/{userId} and verifies token/user mismatch returns 401. */
	@Test
	public void updateUser_tokenUserMismatch_unauthorized() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You can only update your own user."))
				.given(userService).updateUser(2L, "valid-token", "Updated bio", null);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 2L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
	}

	/** Tests PUT /users/{userId} and verifies a non-existent target user returns 404. */
	@Test
	public void updateUser_targetUserNotFound_notFound() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("Updated bio");

		willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id 999 was not found."))
				.given(userService).updateUser(999L, "valid-token", "Updated bio", null);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 999L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isNotFound());
	}

	/** Tests PUT /users/{userId} and verifies a too-short new password returns 400. */
	@Test
	public void updateUser_shortPassword_badRequest() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setNewPassword("short");

		willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password must be at least 8 characters long."))
				.given(userService).updateUser(1L, "valid-token", null, "short");

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isBadRequest());
	}

	/** Tests PUT /users/{userId} and verifies a too-long bio returns 400. */
	@Test
	public void updateUser_tooLongBio_badRequest() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();
		userUpdatePutDTO.setBio("a".repeat(281));

		willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The bio must be at most 280 characters long."))
				.given(userService).updateUser(1L, "valid-token", "a".repeat(281), null);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isBadRequest());
	}

	/** Tests PUT /users/{userId} and verifies an empty update request returns 400. */
	@Test
	public void updateUser_noFields_badRequest() throws Exception {
		UserUpdatePutDTO userUpdatePutDTO = new UserUpdatePutDTO();

		willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one updatable field must be provided."))
				.given(userService).updateUser(1L, "valid-token", null, null);

		MockHttpServletRequestBuilder putRequest = put("/users/{userId}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token")
				.content(asJsonString(userUpdatePutDTO));

		mockMvc.perform(putRequest).andExpect(status().isBadRequest());
	}

	/** Tests POST /users and verifies omitted bio defaults to an empty string in the response (201 status). */
	@Test
	public void createUser_missingBio_defaultsToEmptyString() throws Exception {
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setBio("");
		user.setPasswordHash("$2a$10$M6Q4j0c5xmq5eS7z7hSI6eqWQ2F/N8z6p10tmSMx8nggKQWQqTKe2");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		// bio intentionally omitted

		given(userService.createUser(Mockito.any(), Mockito.anyString())).willReturn(user);

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.bio", is("")));
	}

	/** Tests POST /users and verifies a too-short password returns 400 Bad Request. */
	@Test
	public void createUser_shortPassword_badRequest() throws Exception {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("short");
		userPostDTO.setBio("Short bio");

		given(userService.createUser(Mockito.any(), Mockito.anyString()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password must be at least 8 characters long."));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	/** Tests POST /users and verifies a duplicate username returns 409 Conflict. */
	@Test
	public void createUser_duplicateUsername_conflict() throws Exception {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("Short bio");

		given(userService.createUser(Mockito.any(), Mockito.anyString()))
				.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "The username provided is not unique."));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isConflict());
	}

	/** Tests POST /users and verifies a blank name returns 400 Bad Request. */
	@Test
	public void createUser_blankName_badRequest() throws Exception {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName(" ");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("Short bio");

		given(userService.createUser(Mockito.any(), Mockito.anyString()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The name must not be empty."));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	/** Tests POST /users and verifies a bio longer than 280 characters returns 400. */
	@Test
	public void createUser_tooLongBio_badRequest() throws Exception {
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("a".repeat(281));

		given(userService.createUser(Mockito.any(), Mockito.anyString()))
				.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "The bio must be at most 280 characters long."));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest());
	}

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this:
	 * {"name":"Test User", "username":"testUsername", "password":"password123", "bio":"Short bio"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
