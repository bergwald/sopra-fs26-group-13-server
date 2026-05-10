package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.constant.SearchRegion;
import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;

public class SessionServiceTest {

    private GameDataRepository gameDataRepository;
    private SessionRepository sessionRepository;
    private SessionUserRepository sessionUserRepository;
    private UserRepository userRepository;
    private GooglePanoramaService googlePanoramaService;

    private SessionService sessionService;

    private Session mockSession;

    private User mockUser;
    private SessionUser mockSessionUser;

    private List<SearchRegion> mockSearchRegions;

    @BeforeEach
    void setUp() {

        // Somehow the marking as mock and injecting mock didnt work properly
        gameDataRepository = mock(GameDataRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionUserRepository = mock(SessionUserRepository.class);
        userRepository = mock(UserRepository.class);
        googlePanoramaService = mock(GooglePanoramaService.class);

        // Manually inject mocks into the session service
        sessionService = new SessionService(
                gameDataRepository,
                sessionRepository,
                sessionUserRepository,
                userRepository,
                googlePanoramaService);

        // Setup mock data
        mockSession = new Session();
        mockSession.setRoundNumber(0);
        mockSession.setId(UUID.randomUUID());
        mockSession.setSessionExpiryDateTime(LocalDateTime.now().plusHours(2));

        mockUser = new User();
        mockUser.setId(1L);

        mockSessionUser = new SessionUser();
        mockSessionUser.setUser(mockUser);
        mockSessionUser.setSession(mockSession);

        mockSearchRegions = List.of(new SearchRegion("Alps", 6.0, 45.0, 7.0, 46.0),
                new SearchRegion("Alps", 1.0, 42.0, 4.0, 46.0),
                new SearchRegion("Alps", 1.0, 42.0, 4.0, 46.0));

    }

    @Test
    void testGetAllSessions() {
        List<Session> mockSessions = List.of(mockSession);
        when(sessionRepository.findAll()).thenReturn(mockSessions);

        List<Session> sessions = sessionService.getAllSessions();

        assertEquals(1, sessions.size());
        assertEquals(mockSession, sessions.get(0));
    }

    @Test
    void testCreateNewSession() {
        when(sessionRepository.save(any(Session.class))).thenReturn(mockSession);

        Session newSession = sessionService.createNewSession(0L);

        assertNotNull(newSession);
        assertEquals(0, newSession.getRoundNumber());
        assertEquals(LocalDateTime.now().plusHours(2).toLocalDate().toString(),
                newSession.getSessionExpiryDateTime().toLocalDate().toString());
    }

    @Test
    void testUserJoinSession_valid() {
        when(sessionRepository.findById(any(UUID.class))).thenReturn(mockSession);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(sessionUserRepository.save(any(SessionUser.class))).thenReturn(mockSessionUser);

        Session joinedSession = sessionService.userJoinSession(1L, mockSession.getId(), UserSessionRole.PLAYER);

        assertNotNull(joinedSession);
        assertEquals(mockSession, joinedSession);
        verify(sessionUserRepository, times(1)).save(any(SessionUser.class));
    }

    @Test
    void testUserJoinSession_invalidSessionId() {
        when(sessionRepository.findById(any(UUID.class))).thenReturn(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(sessionUserRepository.save(any(SessionUser.class))).thenReturn(mockSessionUser);

        ResponseStatusException responseException = assertThrows(ResponseStatusException.class,
                () -> sessionService.userJoinSession(1L, UUID.randomUUID(), UserSessionRole.OWNER));

        assertEquals(HttpStatus.NOT_FOUND, responseException.getStatusCode());
    }

    @Test
    void testUserJoinSession_invalidUserId() {
        when(sessionRepository.findById(any(UUID.class))).thenReturn(mockSession);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseStatusException responseException = assertThrows(ResponseStatusException.class,
                () -> sessionService.userJoinSession(1L, mockSession.getId(), UserSessionRole.OWNER));

        assertEquals(HttpStatus.NOT_FOUND, responseException.getStatusCode());
    }

    @Test
    void testUserJoinSession_ongoingSession() {
        mockSession.setRoundNumber(1);
        when(sessionRepository.findById(any(UUID.class))).thenReturn(mockSession);
        when(sessionUserRepository.save(any(SessionUser.class))).thenReturn(mockSessionUser);

        ResponseStatusException responseException = assertThrows(ResponseStatusException.class,
                () -> sessionService.userJoinSession(1L, UUID.randomUUID(), UserSessionRole.PLAYER));

        assertEquals(HttpStatus.FORBIDDEN, responseException.getStatusCode());
    }

    @Test
    void testGetAllSessionUser_valid() {
        when(sessionUserRepository.findBySessionId(any(UUID.class)))
                .thenReturn(Collections.singletonList(mockSessionUser));

        List<SessionUser> foundSessionUser = sessionService.getAllSessionUser(mockSession.getId());
        assertNotNull(foundSessionUser);
        assertEquals(mockSessionUser.getId(), foundSessionUser.get(0).getId());
        assertEquals(mockSessionUser.getScore(), foundSessionUser.get(0).getScore());
        assertEquals(mockSessionUser.getUserRole(), foundSessionUser.get(0).getUserRole());

    }

    @Test
    void testGetAllSessionUser_invalid() {
        when(sessionUserRepository.findBySessionId(any(UUID.class))).thenReturn(Collections.emptyList());

        ResponseStatusException responseException = assertThrows(ResponseStatusException.class,
                () -> sessionService.getAllSessionUser(mockSession.getId()));

        assertEquals(HttpStatus.NOT_FOUND, responseException.getStatusCode());
        assertEquals("No user to session associated found!", responseException.getReason());
    }

    @Test
    void testResetCoordinatesOfSessionUsers_success() {
        mockSessionUser.setGuessLatitude(99);
        mockSessionUser.setGuessLongitude(77);
        when(sessionUserRepository.findBySessionId(any(UUID.class)))
                .thenReturn(Collections.singletonList(mockSessionUser));

        sessionService.resetCoordinatesOfSessionUsers(mockSession.getId());
        assertEquals(-1, mockSessionUser.getGuessLatitude());
        assertEquals(-1, mockSessionUser.getGuessLongitude());

    }

    @Test
    void testResetUserGuessSubmitted() {
        mockSessionUser.setGuessSubmitted(true);
        when(sessionUserRepository.findBySessionId(any(UUID.class)))
                .thenReturn(Collections.singletonList(mockSessionUser));

        sessionService.resetUserGuessSubmitted(mockSession.getId());
        assertEquals(false, mockSessionUser.getGuessSubmitted());
    }

    @Test
    void testValidateSessionExpiryDate_valid() {
        Session returnedSession = sessionService.validateSessionExpiryDate(mockSession);
        assertEquals(returnedSession, mockSession);
    }

    @Test
    void testValidateSessionExpiryDate_invalid() {
        mockSession.setSessionExpiryDateTime(LocalDateTime.now().plusHours(2).plusMinutes(1));
        doNothing().when(sessionUserRepository).delete(any(SessionUser.class));
        doNothing().when(sessionRepository).delete(any(Session.class));

        ResponseStatusException responseException = assertThrows(ResponseStatusException.class,
                () -> sessionService.validateSessionExpiryDate(mockSession));
        assertEquals(HttpStatus.FORBIDDEN, responseException.getStatusCode());
    }

    @Test
    void testIncreaseSessionExpiryDate() {
        LocalDateTime beforeCall = LocalDateTime.now();

        Session result = sessionService.increaseSessionExpiryDate(mockSession);

        LocalDateTime afterCall = LocalDateTime.now();

        assertTrue(result.getSessionExpiryDateTime().isAfter(beforeCall.plusHours(2)));
        assertTrue(result.getSessionExpiryDateTime().isBefore(afterCall.plusHours(2).plusSeconds(1)));
        assertEquals(result, mockSession);
    }

    @Test
    void testDeleteSession_success() {
        List<SessionUser> mockedSessionUsers = Collections.singletonList(mockSessionUser);
        when(sessionUserRepository.findBySessionId(mockSession.getId())).thenReturn(mockedSessionUsers);
        when(sessionService.getAllSessionUser(mockSession.getId())).thenReturn(mockedSessionUsers);
        doNothing().when(sessionUserRepository).delete(mockSessionUser);
        doNothing().when(sessionUserRepository).flush();
        doNothing().when(sessionRepository).delete(mockSession);
        doNothing().when(sessionRepository).flush();

        boolean result = sessionService.deleteSession(mockSession);

        verify(sessionUserRepository).delete(mockSessionUser);
        verify(sessionUserRepository).flush();
        verify(sessionRepository).delete(mockSession);
        verify(sessionRepository).flush();
        assertTrue(result);
    }

    @Test
    void testDeleteSession_failNoSessionUserFound() {
        doNothing().when(sessionUserRepository).delete(mockSessionUser);
        doNothing().when(sessionUserRepository).flush();
        doNothing().when(sessionRepository).delete(mockSession);
        doNothing().when(sessionRepository).flush();

        boolean result = sessionService.deleteSession(mockSession);

        assertFalse(result);
    }

    @Test
    void testGetAllSessionUserForAuthorizedUser_valid() {
        List<SessionUser> mockedSessionUsers = Collections.singletonList(mockSessionUser);

        when(sessionUserRepository.findById(mockSessionUser.getUser().getId()))
                .thenReturn(Optional.of(mockSessionUser));
        when(sessionUserRepository.findBySessionId(mockSession.getId())).thenReturn(mockedSessionUsers);
        when(sessionService.getAllSessionUser(mockSession.getId())).thenReturn(mockedSessionUsers);
        List<SessionUser> foundSessionUser = sessionService.getAllSessionUserForAuthorizedUser(
                mockSessionUser.getSession().getId(), mockSessionUser.getUser().getId());
        assertEquals(mockedSessionUsers, foundSessionUser);
    }

    @Test
    void testGetAllSessionUserForAuthorizedUser_invalid() {
        List<SessionUser> mockedSessionUsers = Collections.singletonList(mockSessionUser);
        when(sessionUserRepository.findBySessionId(mockSession.getId())).thenReturn(mockedSessionUsers);
        when(sessionService.getAllSessionUser(mockSession.getId())).thenReturn(mockedSessionUsers);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.getAllSessionUserForAuthorizedUser(mockSessionUser.getSession().getId(), 123L);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Session user not found"));
    }

    @Test
    void testInitializeGameData_valid() {

        GooglePanoramaCandidate mockCandidate = mock(GooglePanoramaCandidate.class);
        when(mockCandidate.panoId()).thenReturn("panoId123");
        when(mockCandidate.latitude()).thenReturn(40.7128);
        when(mockCandidate.longitude()).thenReturn(-74.0060);

        when(googlePanoramaService.getSearchRegionsFromString(anyString())).thenReturn(mockSearchRegions);
        when(googlePanoramaService.fetchPanoramaCandidate(any())).thenReturn(mockCandidate);

        sessionService.initializeGameDate(mockSession, "Alps");

        verify(gameDataRepository, times(3)).save(any(Game_data.class));
        verify(googlePanoramaService, times(3)).fetchPanoramaCandidate(mockSearchRegions);
        verify(gameDataRepository).flush();
    }

    @Test
    void testCheckIfUserIsSessionOwner_userIsOwner() {
        mockSessionUser.setUserRole(UserSessionRole.OWNER);
        when(sessionUserRepository.findByUserIdAndSessionId(mockSessionUser.getUser().getId(),
                mockSessionUser.getSession().getId()))
                .thenReturn(Optional.of(mockSessionUser));

        boolean result = sessionService.checkIfUserIsSessionOwner(mockSessionUser.getUser().getId(),
                mockSessionUser.getSession().getIdAsString());

        assertTrue(result);
    }

    @Test
    void testCheckIfUserIsSessionOwner_userIsNotOwner() {
        mockSessionUser.setUserRole(UserSessionRole.PLAYER);
        when(sessionUserRepository.findByUserIdAndSessionId(mockSessionUser.getUser().getId(),
                mockSessionUser.getSession().getId()))
                .thenReturn(Optional.of(mockSessionUser));

        boolean result = sessionService.checkIfUserIsSessionOwner(mockSessionUser.getUser().getId(),
                mockSessionUser.getSession().getIdAsString());

        assertFalse(result);
    }

    @Test
    void testCheckIfUserIsSessionOwner_sessionUserNotFound() {
        UUID sessionUuid = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        when(sessionUserRepository.findByUserIdAndSessionId(99999L, sessionUuid))
                .thenReturn(Optional.empty());

        boolean result = sessionService.checkIfUserIsSessionOwner(99999L, sessionUuid.toString());

        assertFalse(result);
    }

    @Test
    void testGetSessionWithId_valid() {
        when(sessionRepository.findById(mockSession.getId())).thenReturn(mockSession);
        Session foundSession = sessionService.getSessionWithId(mockSession.getIdAsString());
        assertEquals(mockSession, foundSession);
    }

    @Test
    void testGetSessionWithId_sessionNotFoundWithMessage() {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.getSessionWithId("invalid-session");
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testIncreaseSessionRoundNumber_validIncrease() {
        mockSession.setRoundNumber(1);
        int result = sessionService.increaseSessionRoundNumber(mockSession, mockSession.getRoundNumber(), 5);

        assertEquals(2, result);
        assertEquals(2, mockSession.getRoundNumber());
        assertNotNull(mockSession.getRoundStartedDateTime());
        verify(sessionRepository).save(mockSession);
    }

    @Test
    void testIncreaseSessionRoundNumber_validLastRound() {
        mockSession.setRoundNumber(5);
        int result = sessionService.increaseSessionRoundNumber(mockSession, mockSession.getRoundNumber(), 5);

        assertEquals(6, result);
        assertEquals(6, mockSession.getRoundNumber());
        assertNotNull(mockSession.getRoundStartedDateTime());
        verify(sessionRepository).save(mockSession);
    }

    @Test
    void testIncreaseSessionRoundNumber_invalidHighRoundNumber() {
        mockSession.setRoundNumber(99);
        int result = sessionService.increaseSessionRoundNumber(mockSession, mockSession.getRoundNumber(), 5);

        assertEquals(6, result);
        assertEquals(6, mockSession.getRoundNumber());
        assertNotNull(mockSession.getRoundStartedDateTime());
        verify(sessionRepository).save(mockSession);
    }

    @Test
    void testValidateSessionGameGuess_valid() {
        mockSession.setRoundNumber(1);
        when(sessionRepository.findById(mockSession.getId())).thenReturn(mockSession);
        sessionService.validateSessionGameGuess(mockSession.getIdAsString(), mockSession.getRoundNumber());
        verify(sessionRepository).findById(mockSession.getId());
    }

    @Test
    void testValidateSessionGameGuess_invalidSession() {
        when(sessionRepository.findById(mockSession.getId())).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.validateSessionGameGuess(mockSession.getIdAsString(), mockSession.getRoundNumber());
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testValidateSessionGameGuess_invalidRoundNumber() {
        mockSession.setRoundNumber(1);
        when(sessionRepository.findById(mockSession.getId())).thenReturn(mockSession);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.validateSessionGameGuess(mockSession.getIdAsString(), 2);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Session round is out of sync", exception.getReason());

    }

    @Test
    void testValidateSessionGameGuess_invalidRoundNumberBelowOne() {
        mockSession.setRoundNumber(0);
        when(sessionRepository.findById(mockSession.getId())).thenReturn(mockSession);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.validateSessionGameGuess(mockSession.getIdAsString(), mockSession.getRoundNumber());
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid round number", exception.getReason());

    }

    @Test
    void testValidateSessionGameGuess_invalidRoundNumberTooHighRound() {
        mockSession.setRoundNumber(999);
        when(sessionRepository.findById(mockSession.getId())).thenReturn(mockSession);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            sessionService.validateSessionGameGuess(mockSession.getIdAsString(), mockSession.getRoundNumber());
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid round number", exception.getReason());

    }

    @Test
    void testCleanUpSessionBeforeCreating_nonOwnerDeletesSessionUser() {
        SessionUser su = new SessionUser();
        su.setUserRole(UserSessionRole.PLAYER);
        when(sessionUserRepository.findById(mockUser.getId())).thenReturn(Optional.of(su));

        sessionService.cleanUpSessionBeforeCreating(mockUser.getId());

        verify(sessionUserRepository).delete(su);
        verify(sessionUserRepository).flush();
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void testCleanUpSessionBeforeCreating_notFoundDoesNothing() {
        when(sessionUserRepository.findById(mockUser.getId())).thenReturn(Optional.empty());

        sessionService.cleanUpSessionBeforeCreating(mockUser.getId());

        verify(sessionUserRepository, never()).delete(any());
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    void testCleanUpSessionBeforeCreating_userRoleOwner() {
        SessionUser su = new SessionUser();
        su.setUserRole(UserSessionRole.OWNER);
        su.setSession(mockSession);
        List<SessionUser> mockedSessionUsers = Collections.singletonList(su);
        when(sessionUserRepository.findBySessionId(mockSession.getId())).thenReturn(mockedSessionUsers);
        when(sessionService.getAllSessionUser(mockSession.getId())).thenReturn(mockedSessionUsers);

        when(sessionUserRepository.findById(mockUser.getId())).thenReturn(Optional.of(su));

        sessionService.cleanUpSessionBeforeCreating(mockUser.getId());

        doNothing().when(sessionUserRepository).delete(mockSessionUser);
        doNothing().when(sessionUserRepository).flush();
        doNothing().when(sessionRepository).delete(su.getSession());
        doNothing().when(sessionRepository).flush();

        verify(sessionUserRepository).delete(su);
        verify(sessionUserRepository).flush();
        verify(sessionRepository).delete(su.getSession());
        verify(sessionRepository).flush();
    }

}
