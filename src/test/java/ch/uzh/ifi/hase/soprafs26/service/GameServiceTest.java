package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

public class GameServiceTest {

	private GameDataRepository gameDataRepository;
	private SessionUserRepository sessionUserRepository;
	private SessionRepository sessionRepository;
	private GameService gameService;

	@Test
	public void getSessionRoundData_valid() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		Game_data gameDataOut = new Game_data();
		gameDataOut.setSessionId("SessionId1234");
		gameDataOut.setRoundNumber(2);
		gameDataOut.setImageUrl("wikidata.com/nr");
		gameDataOut.setLatitude(1.0f);
		gameDataOut.setLongitude(4.0f);
		when(gameDataRepository.findBySessionIdAndRoundNumber(anyString(), anyInt())).thenReturn(gameDataOut);

		Game_data mockGameDataIn = new Game_data();
		mockGameDataIn.setSessionId("SessionId1234");
		mockGameDataIn.setRoundNumber(2);
		Game_data actualgameDataOut = gameService.getSessionRoundData(mockGameDataIn);

		assertEquals(actualgameDataOut.getSessionId(), gameDataOut.getSessionId());
		assertEquals(actualgameDataOut.getRoundNumber(), gameDataOut.getRoundNumber());
	}

	@Test
	public void saveScore_addsRoundScoreToExistingTotal() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
		User user = new User();
		user.setId(9L);
		Session session = new Session();
		session.setId(sessionId);
		SessionUser sessionUser = new SessionUser();
		sessionUser.setUser(user);
		sessionUser.setSession(session);
		sessionUser.setScore(10L);

		when(sessionUserRepository.findByUserIdAndSessionId(9L, sessionId))
				.thenReturn(Optional.of(sessionUser));

		gameService.saveScore(9L, 7, sessionId.toString());

		assertEquals(17L, sessionUser.getScore().longValue());
		verify(sessionUserRepository).save(sessionUser);
	}

	@Test
	public void saveScore_invalidSessionId_throwsBadRequest() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> gameService.saveScore(1L, 5, "not-a-uuid"));
		assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
	}

	@Test
	public void saveScore_sessionUserMissing_throwsNotFound() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("22222222-2222-2222-2222-222222222222");
		when(sessionUserRepository.findByUserIdAndSessionId(1L, sessionId)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> gameService.saveScore(1L, 5, sessionId.toString()));
		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
	}

	@Test
	public void saveScore_returnsPersistedScore() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333");
		SessionUser sessionUser = new SessionUser();
		sessionUser.setScore(42L);
		when(sessionUserRepository.findByUserIdAndSessionId(3L, sessionId))
				.thenReturn(Optional.of(sessionUser));

		assertEquals(52L, gameService.saveScore(3L, 10, sessionId.toString()));
	}

	@Test
	public void getSessionRoundDataForUser_requiresMembership() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("44444444-4444-4444-4444-444444444444");
		when(sessionUserRepository.findByUserIdAndSessionId(1L, sessionId)).thenReturn(Optional.empty());

		ResponseStatusException ex = assertThrows(ResponseStatusException.class,
				() -> gameService.getSessionRoundDataForUser(1L, sessionId.toString(), 1));
		assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
	}

	@Test
	public void advanceSinglePlayerRound_movesToNextRound() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("55555555-5555-5555-5555-555555555555");
		Session session = new Session();
		session.setId(sessionId);
		session.setRoundNumber(2);
		when(sessionRepository.findById(sessionId)).thenReturn(session);

		assertEquals(3, gameService.advanceSinglePlayerRound(sessionId.toString(), 2));
		assertEquals(3, session.getRoundNumber());
		verify(sessionRepository).save(session);
	}

	@Test
	public void advanceSinglePlayerRound_marksSessionFinishedAfterFinalRound() {
		gameDataRepository = mock(GameDataRepository.class);
		sessionUserRepository = mock(SessionUserRepository.class);
		sessionRepository = mock(SessionRepository.class);
		gameService = new GameService(gameDataRepository, sessionUserRepository, sessionRepository);

		UUID sessionId = UUID.fromString("66666666-6666-6666-6666-666666666666");
		Session session = new Session();
		session.setId(sessionId);
		session.setRoundNumber(3);
		when(sessionRepository.findById(sessionId)).thenReturn(session);

		assertEquals(4, gameService.advanceSinglePlayerRound(sessionId.toString(), 3));
		assertEquals(4, session.getRoundNumber());
	}
}
