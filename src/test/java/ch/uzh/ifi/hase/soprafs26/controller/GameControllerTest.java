package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.GuessEvaluationService;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(GameController.class)
public class GameControllerTest {

    private final ControllerTestHelper controllerTestHelper = new ControllerTestHelper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

	@MockitoBean
	private GuessEvaluationService guessEvaluationService;

	@MockitoBean
	private UserService userService;

        @MockitoBean
        private SessionService sessionService;


	private User sampleUser() {
                User user = new User();
                user.setId(1L);
                user.setUsername("testUsername");
                user.setBio("Some bio");
                user.setPasswordHash("123213123");
                user.setToken("1");
                return user;
    }

    /* TODO: FIX TEST */
    @Test
    void givenAuthorizedUser_whenGetGameData_thenReturnJson() throws Exception {
        Game_data gameData = new Game_data();
        gameData.setSessionId("SessionId1234");
        gameData.setImageUrl("https://example.com/panorama");
        gameData.setLongitude(1.0f);
        gameData.setLatitude(4.0f);
        gameData.setRoundNumber(3);

        Session session = new Session();
        session.setRoundNumber(1);
        session.setRoundStartedDateTime(LocalDateTime.now());
        session.setSessionExpiryDateTime(LocalDateTime.now().plusHours(1));

        given(userService.extractBearerToken(anyString())).willReturn("valid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(gameService.getSessionRoundDataForUser(1L, "SessionId1234", 3)).willReturn(gameData);
        given(sessionService.getSessionWithId("SessionId1234")).willReturn(session);

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .queryParam("sessionId", "SessionId1234")
                .queryParam("roundNumber", "3")
                .header("Authorization", "Bearer valid-token")
                .header("userId", 1L);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", is(gameData.getImageUrl())))
                .andExpect(jsonPath("$.roundNumber", is(gameData.getRoundNumber())))
                .andExpect(jsonPath("$.sessionId", is(gameData.getSessionId())))
                .andExpect(jsonPath("$.roundStartedDateTime",
                        is(ch.uzh.ifi.hase.soprafs26.rest.mapper.ApiDateTimeFormatter
                                .toUtcIsoString(session.getRoundStartedDateTime()))));
    }
/*
    @Test
    void givenUnauthorizedUser_whenGetGameData_thenReturnUnauthorized() throws Exception {
        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .queryParam("sessionId", "SessionId1234")
                .queryParam("roundNumber", "3")
                .header("Authorization", "Bearer invalid-token")
                .header("userId", 1L);
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }
*/
	@Test
	public void makeGuess_200() throws Exception {
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
		given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		Game_data gameData = new Game_data();
		gameData.setLatitude(10.0f);
		gameData.setLongitude(20.0f);
		given(gameService.getSessionRoundDataForUser(1L, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 1))
				.willReturn(gameData);

		given(guessEvaluationService.computeDistanceKm(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
				.willReturn(100.0);
		given(guessEvaluationService.computeScore(100.0)).willReturn(40);
		given(gameService.saveScore(1L, 40, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).willReturn(140L);
		given(gameService.validateSessionGameGuess("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 1)).willReturn(2);

		UserGuessPutDTO body = new UserGuessPutDTO();
		body.setUserId(1L);
		body.setSessionId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		body.setRoundNumber(1);
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

		verify(gameService).saveScore(1L, 40, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		verify(gameService).validateSessionGameGuess("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 1);
		verify(userService).recordPlayedRound(1L, 100.0, 40);
	}

	@Test
	public void makeGuess_userIdMismatch_403() throws Exception {
		given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
		given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		UserGuessPutDTO body = new UserGuessPutDTO();
		body.setUserId(99L);
		body.setSessionId("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
		body.setRoundNumber(1);
		body.setLatitude(1.0);
		body.setLongitude(2.0);

		mockMvc.perform(put("/submit_guess")
				.header("Authorization", "Bearer token")
				.header("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(body)))
				.andExpect(status().isForbidden());
	}

        @Test
        public void makeGuess_roundNuberNull_400() throws Exception {
                given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
		given(userService.extractBearerToken(anyString())).willReturn("valid-token");

		UserGuessPutDTO body = new UserGuessPutDTO();
		body.setUserId(1L);
		body.setSessionId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		body.setLatitude(11.0);
		body.setLongitude(21.0);

		MockHttpServletRequestBuilder putRequest = put("/submit_guess")
				.header("Authorization", "Bearer token")
				.header("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(body));

		mockMvc.perform(putRequest)
				.andExpect(status().isBadRequest());
        }       

        @Test
        public void makeGuess_LatLongNotSet() throws Exception {
                UserGuessPutDTO userGuess = new UserGuessPutDTO();
                userGuess.setUserId(1L);
		userGuess.setSessionId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
		userGuess.setRoundNumber(1);
		userGuess.setLatitude(-1.0);
		userGuess.setLongitude(-1.0);

                Game_data gameData = new Game_data();
		gameData.setLatitude(10.0f);
		gameData.setLongitude(20.0f);

		given(gameService.getSessionRoundDataForUser(1L, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 1))
				.willReturn(gameData);
		given(gameService.validateSessionGameGuess("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", 1)).willReturn(2);
		given(guessEvaluationService.computeScore(0)).willReturn(0);
		given(gameService.saveScore(1L, 0, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")).willReturn(0L);
                
                MockHttpServletRequestBuilder putRequest = put("/submit_guess")
				.header("Authorization", "Bearer token")
				.header("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userGuess));

		mockMvc.perform(putRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scoreRound", is(0)))
				.andExpect(jsonPath("$.distance", is(-1.0)));

		verify(userService).recordPlayedRound(1L, -1.0, 0);
        }
        @Test
        void givenMissingGameData_whenGetGameData_thenReturnNotFound() throws Exception {
                given(userService.extractBearerToken(anyString())).willReturn("valid-token");
                given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
                given(gameService.getSessionRoundDataForUser(1L, "SessionId1234", 3))
                        .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game data not found"));

                MockHttpServletRequestBuilder getRequest = get("/game_data")
                        .queryParam("sessionId", "SessionId1234")
                        .queryParam("roundNumber", "3")
                        .header("Authorization", "Bearer valid-token")
                        .header("userId", 1L);

                mockMvc.perform(getRequest).andExpect(status().isNotFound());
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
