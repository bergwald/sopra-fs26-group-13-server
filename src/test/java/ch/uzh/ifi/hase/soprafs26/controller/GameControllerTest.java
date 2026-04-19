package ch.uzh.ifi.hase.soprafs26.controller;

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

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    private final ControllerTestHelper controllerTestHelper = new ControllerTestHelper();

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

    @Test
    void givenAuthorizedUser_whenGetGameData_thenReturnJson() throws Exception {
        GameGetDTO requestBody = new GameGetDTO();
        requestBody.setSessionId("SessionId1234");
        requestBody.setRoundNumber(3);

        Game_data gameData = new Game_data();
        gameData.setSessionId("SessionId1234");
        gameData.setImageUrl("https://example.com/panorama");
        gameData.setLongitude(1.0f);
        gameData.setLatitude(4.0f);
        gameData.setRoundNumber(3);

        given(userService.extractBearerToken(anyString())).willReturn("valid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(gameService.getSessionRoundData(any(Game_data.class))).willReturn(gameData);

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(controllerTestHelper.asJsonString(requestBody))
                .header("Authorization", "Bearer valid-token")
                .header("userId", 1L);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", is(gameData.getImageUrl())))
                .andExpect(jsonPath("$.roundNumber", is(gameData.getRoundNumber())))
                .andExpect(jsonPath("$.sessionId", is(gameData.getSessionId())));
    }

    @Test
    void givenUnauthorizedUser_whenGetGameData_thenReturnUnauthorized() throws Exception {
        GameGetDTO requestBody = new GameGetDTO();
        requestBody.setSessionId("SessionId1234");
        requestBody.setRoundNumber(3);

        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(controllerTestHelper.asJsonString(requestBody))
                .header("Authorization", "Bearer invalid-token")
                .header("userId", 1L);

        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void givenMissingGameData_whenGetGameData_thenReturnNotFound() throws Exception {
        GameGetDTO requestBody = new GameGetDTO();
        requestBody.setSessionId("SessionId1234");
        requestBody.setRoundNumber(3);

        given(userService.extractBearerToken(anyString())).willReturn("valid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(gameService.getSessionRoundData(any(Game_data.class)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game data not found"));

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(controllerTestHelper.asJsonString(requestBody))
                .header("Authorization", "Bearer valid-token")
                .header("userId", 1L);

        mockMvc.perform(getRequest).andExpect(status().isNotFound());
    }
}
