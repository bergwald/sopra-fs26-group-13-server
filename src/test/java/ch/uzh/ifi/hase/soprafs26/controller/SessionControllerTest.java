package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPutDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

@WebMvcTest(SessionController.class)
public class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SessionService sessionService;

    private Session sampleSession() {
        Session session = new Session();
        UUID testUuid = UUID.fromString("12345678-1234-1234-4567-123456789123");
        session.setId(testUuid);
        session.setRoundNumber(0);
        session.setSessionExpiryDateTime(LocalDateTime.of(2026, 1, 1, 8, 1, 1, 1));
        return session;
    }

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
    public void givenSessions_whenGetSession_thenReturnJsonArray() throws Exception {
        Session session = sampleSession();
        List<Session> allSessions = Collections.singletonList(session);
        given(sessionService.getAllSessions()).willReturn(allSessions);
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("valid-token");

        MockHttpServletRequestBuilder getRequest = get("/session").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token").header("userId", 1);

        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(session.getId().toString())))
                .andExpect(jsonPath("$[0].roundNumber", is(session.getRoundNumber())))
                .andExpect(jsonPath("$[0].sessionExpiryDateTime", is(session.getSessionExpiryDateTime().toString())));
    }

    @Test
    public void givenMultipleSessions_whenGetSession_thenReturnJsonArrayWithMultipleSessions() throws Exception {
        Session session1 = sampleSession();
        Session session2 = sampleSession();

        List<Session> allSessions = new ArrayList<>();
        allSessions.add(session1);
        allSessions.add(session2);
        given(sessionService.getAllSessions()).willReturn(allSessions);
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("valid-token");

        MockHttpServletRequestBuilder getRequest = get("/session").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer valid-token").header("userId", 1);

        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void givenUnauthorizedUser_whenGetSession_thenReturnUnauthorized() throws Exception {
        Session session = sampleSession();
        List<Session> allSessions = Collections.singletonList(session);

        given(sessionService.getAllSessions()).willReturn(allSessions);
        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        MockHttpServletRequestBuilder getRequest = get("/session").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer invalid-token").header("userId", 1);
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void givenEmptyAuthrozation_whenGetSession_thenReturnUnauthorized() throws Exception {
        Session session = sampleSession();
        List<Session> allSessions = Collections.singletonList(session);

        given(sessionService.getAllSessions()).willReturn(allSessions);
        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(null, null))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        MockHttpServletRequestBuilder getRequest = get("/session").contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void givenAuthorizedUser_whenPostSession_thenReturnJsonOfCreatedSession() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(userService.extractBearerToken(anyString())).willReturn("valid-token");
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPostDTO sessionPost = new SessionPostDTO();
        sessionPost.setUserId(1L);

        MockHttpServletRequestBuilder postRequest = post("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPost))
                .header("Authorization", "Bearer valid-token").header("userId", 1);
        mockMvc.perform(postRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(session.getId().toString())))
                .andExpect(jsonPath("$.roundNumber", is(session.getRoundNumber())))
                .andExpect(jsonPath("$.sessionExpiryDateTime", is(session.getSessionExpiryDateTime().toString())));
    }

    @Test
    public void givenUnauthorizedUser_whenPostSession_thenThrowUnauthorizedError() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);

        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPostDTO sessionPost = new SessionPostDTO();
        sessionPost.setUserId(1L);

        MockHttpServletRequestBuilder postRequest = post("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPost))
                .header("Authorization", "Bearer invalid-token").header("userId", 1);
        mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void givenEmptyAuthorization_whenPostSession_thenThrowUnauthorizedError() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);

        given(userService.extractBearerToken(anyString())).willReturn(null);
        given(userService.getAuthorizedTargetUser(null, null))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPostDTO sessionPost = new SessionPostDTO();
        sessionPost.setUserId(1L);

        MockHttpServletRequestBuilder postRequest = post("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPost));
        mockMvc.perform(postRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void givenAuthorizedUser_whenPutSession_thenReturnJsonOfJoinedSession() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);

        given(userService.extractBearerToken(anyString())).willReturn("valid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString())).willReturn(sampleUser());
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPutDTO sessionPut = new SessionPutDTO();
        sessionPut.setUserId(1L);
        sessionPut.setSessionId(sampleSession().getId().toString());

        MockHttpServletRequestBuilder putRequest = put("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPut)).header("Authorization", "Bearer valid-token").header("userId", 1);
        ;
        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(session.getId().toString())))
                .andExpect(jsonPath("$.roundNumber", is(session.getRoundNumber())))
                .andExpect(jsonPath("$.sessionExpiryDateTime", is(session.getSessionExpiryDateTime().toString())));
    }

    @Test
    public void givenUnauthorizedUser_whenPutSession_thenThrowUnauthorizedError() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);

        given(userService.extractBearerToken(anyString())).willReturn("invalid-token");
        given(userService.getAuthorizedTargetUser(anyLong(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPutDTO sessionPut = new SessionPutDTO();
        sessionPut.setUserId(1L);
        sessionPut.setSessionId(sampleSession().getId().toString());

        MockHttpServletRequestBuilder putRequest = put("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPut)).header("Authorization", "Bearer invalid-token").header("userId", 1);
        ;
        mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
    }

    @Test
    public void givenEmptyAuthorization_whenPutSession_thenThrowUnauthorizedError() throws Exception {
        Session session = sampleSession();
        given(sessionService.createNewSession()).willReturn(session);

        given(userService.extractBearerToken(anyString())).willReturn(null);
        given(userService.getAuthorizedTargetUser(null, null))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid."));
        given(sessionService.userJoinSession(anyLong(), any())).willReturn(session);

        SessionPutDTO sessionPut = new SessionPutDTO();
        sessionPut.setUserId(1L);
        sessionPut.setSessionId(sampleSession().getId().toString());

        MockHttpServletRequestBuilder putRequest = put("/session").contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(sessionPut));
        mockMvc.perform(putRequest).andExpect(status().isUnauthorized());
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     * Input will look like this:
     * {"name":"Test User", "username":"testUsername", "password":"password123",
     * "bio":"Short bio"}
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