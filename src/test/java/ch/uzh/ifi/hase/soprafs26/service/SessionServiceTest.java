package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

public class SessionServiceTest {

    private SessionRepository sessionRepository;
    private SessionUserRepository sessionUserRepository;
    private UserRepository userRepository;

    private SessionService sessionService;

    private Session mockSession;
    private User mockUser;
    private SessionUser mockSessionUser;

    @BeforeEach
    void setUp() {

        // Somehow the marking as mock and injecting mock didnt work properly
        sessionRepository = mock(SessionRepository.class);
        sessionUserRepository = mock(SessionUserRepository.class);
        userRepository = mock(UserRepository.class);

        // Manually inject mocks into the session service
        sessionService = new SessionService(sessionRepository, sessionUserRepository, userRepository);

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

        Session newSession = sessionService.createNewSession();

        assertNotNull(newSession);
        assertEquals(0, newSession.getRoundNumber());
        assertEquals(LocalDateTime.now().plusHours(2).toLocalDate().toString(),
                newSession.getSessionExpiryDateTime().toLocalDate().toString());
    }

    @Test
    void testUserJoinSession() {
        when(sessionRepository.findById(any(UUID.class))).thenReturn(mockSession);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        when(sessionUserRepository.save(any(SessionUser.class))).thenReturn(mockSessionUser);

        Session joinedSession = sessionService.userJoinSession(1L, mockSession.getId());

        assertNotNull(joinedSession);
        assertEquals(mockSession, joinedSession);
        verify(sessionUserRepository, times(1)).save(any(SessionUser.class));
    }
}
