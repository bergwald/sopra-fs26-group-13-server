package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import static org.mockito.Mockito.when;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

	private GameDataRepository gameDataRepository;
	private GameService gameService;

	
	@Test
	public void getSessionRoundData_valid() {
		//expected Output
		gameDataRepository = mock(GameDataRepository.class);
		gameService = new GameService(gameDataRepository);

		Game_data gameDataOut = new Game_data();
		gameDataOut.setSessionId("SessionId1234");
		gameDataOut.setRoundNumber(2);
		gameDataOut.setImageUrl("wikidata.com/nr");
		gameDataOut.setLatitude(1.0f);
		gameDataOut.setLongitude(4.0f);
		when(gameDataRepository.findBySessionIdAndRoundNumber(anyString(), anyInt())).thenReturn(gameDataOut);

		Game_data mockGameDataIn = new Game_data();
		Game_data actualgameDataOut = gameService.getSessionRoundData(mockGameDataIn);

		assertEquals(actualgameDataOut.getSessionId(), gameDataOut.getSessionId());
		assertEquals(actualgameDataOut.getRoundNumber(), gameDataOut.getRoundNumber());
	}
}
