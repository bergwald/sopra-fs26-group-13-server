package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GameService;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    private final ControllerTestHelper controllerTestHelper = new ControllerTestHelper();

    @Test
    void givenGameData_whenGetGameData_thenReturnBothImageUrlFieldNames() throws Exception {
        GameGetDTO requestBody = new GameGetDTO();
        requestBody.setSessionId("SessionId1234");
        requestBody.setRoundNumber(3);

        Game_data gameData = new Game_data();
        gameData.setSessionId("SessionId1234");
        gameData.setImageUrl("https://example.com/panorama");
        gameData.setRoundNumber(3);

        given(gameService.getGameData(any(Game_data.class))).willReturn(gameData);

        MockHttpServletRequestBuilder getRequest = get("/game_data")
                .contentType(MediaType.APPLICATION_JSON)
                .content(controllerTestHelper.asJsonString(requestBody));

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl", is(gameData.getImageUrl())))
                .andExpect(jsonPath("$.roundNumber", is(gameData.getRoundNumber())))
                .andExpect(jsonPath("$.sessionId", is(gameData.getSessionId())));
    }
}
