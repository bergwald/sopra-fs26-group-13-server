package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

	@Test
	public void givenInvalidUserId_whenGetUser_thenReturnNotFound() throws Exception {
		given(userService.getUserById(999L))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id 999 was not found."));

		MockHttpServletRequestBuilder getRequest = get("/users/{userId}", 999L).contentType(MediaType.APPLICATION_JSON);

		mockMvc.perform(getRequest).andExpect(status().isNotFound());
	}

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
