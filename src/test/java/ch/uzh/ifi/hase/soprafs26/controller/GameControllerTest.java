package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.when;
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
@WebMvcTest(GameController.class)
public class GameControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GameService gameService;

	@MockitoBean
	private UserService userService;

	private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setUsername("testUsername");
        user.setBio("Some bio");
        user.setPasswordHash("123213123");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);
        return user;
    }

	/**
	 * Tests GET /users and verifies it returns all users as a JSON array (200
	 * status).
	 * GIVEN
	 */
	@Test
	public void getSessionRoundURL_200() throws Exception {
		//given
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		Game_data gameData = new Game_data();
		gameData.setSessionId("SessionId1234");
		gameData.setImageUrl("wikidata.com/nr");
		gameData.setLongitude(1.0f);
		gameData.setLatitude(4.0f);
		gameData.setRoundNumber(3);

		GameDataGetDTO gameGet = new GameDataGetDTO();

		given(gameService.getSessionRoundData(any())).willReturn(gameData);
		MockHttpServletRequestBuilder getRequest = get("/game_data").contentType(MediaType.APPLICATION_JSON).content(asJsonString(gameGet));
		mockMvc.perform(getRequest).andExpect(status().isOk())
			.andExpect(jsonPath("$.wikidataUrl", is(gameData.getImageUrl())))
			.andExpect(jsonPath("$.roundNumber", is(gameData.getRoundNumber())))
			.andExpect(jsonPath("$.sessionId", is(gameData.getSessionId())));
	
	}
	@Test
	public void getSessionRoundURL_400() throws Exception {
		//given
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		GameDataGetDTO gameGet = new GameDataGetDTO();

		given(gameService.getSessionRoundData(any())).willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		MockHttpServletRequestBuilder getRequest = get("/game_data").contentType(MediaType.APPLICATION_JSON).content(asJsonString(gameGet));
		mockMvc.perform(getRequest).andExpect(status().isBadRequest());
	
	}

	public void getSessionRoundURL_401() throws Exception {
		//given
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");

		GameDataGetDTO gameGet = new GameDataGetDTO();
		Game_data gameData = new Game_data();

		given(gameService.getSessionRoundData(any())).willReturn(gameData);
		MockHttpServletRequestBuilder getRequest = get("/game_data").contentType(MediaType.APPLICATION_JSON).content(asJsonString(gameGet));
		mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
	
	}
	private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
