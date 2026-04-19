package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.GuessEvaluationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

	@MockitoBean
	private GuessEvaluationService guessEvaluationService;

	@MockitoBean
	private SessionRepository sessionRepository;

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
	@Test
	public void makeGuess_200() throws Exception {
		UUID sessionUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
		given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		Session session = new Session();
		session.setId(sessionUuid);
		session.setRoundNumber(1);
		given(sessionRepository.findById(sessionUuid)).willReturn(session);

		Game_data gameData = new Game_data();
		gameData.setLatitude(10.0f);
		gameData.setLongitude(20.0f);
		given(gameService.getSessionRoundData(any())).willReturn(gameData);

		given(guessEvaluationService.computeDistanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(100.0);
		given(guessEvaluationService.computeScore(100.0)).willReturn(40);
		given(gameService.saveScore(1L, 40, sessionUuid.toString())).willReturn(140L);

		UserGuessPutDTO body = new UserGuessPutDTO();
		body.setUserId(1L);
		body.setSessionId(sessionUuid.toString());
		body.setLatitude(11.0);
		body.setLongitude(21.0);

		MockHttpServletRequestBuilder putRequest = put("/submit_guess")
				.header("Authorization", "Bearer token")
				.header("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(body));

		mockMvc.perform(putRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.latitude", is(closeTo(10.0, 1e-5))))
				.andExpect(jsonPath("$.longitude", is(closeTo(20.0, 1e-5))))
				.andExpect(jsonPath("$.distance", is(closeTo(100.0, 1e-5))))
				.andExpect(jsonPath("$.scoreRound", is(40)))
				.andExpect(jsonPath("$.scoreOverall", is(140)));

		verify(gameService).saveScore(1L, 40, sessionUuid.toString());
	}

	@Test
	public void makeGuess_userIdMismatch_403() throws Exception {
		UUID sessionUuid = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
		given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		UserGuessPutDTO body = new UserGuessPutDTO();
		body.setUserId(99L);
		body.setSessionId(sessionUuid.toString());
		body.setLatitude(1.0);
		body.setLongitude(2.0);

		mockMvc.perform(put("/submit_guess")
				.header("Authorization", "Bearer token")
				.header("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(body)))
				.andExpect(status().isForbidden());
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
