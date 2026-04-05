package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class GameDataRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private GameDataRepository gameDataRepository;

	@Test
	public void findByDataId_success() {
		// given
		Game_data gameData = new Game_data();
		gameData.setSession_id("Session1234");
		gameData.setImage_url("wikipictures.com/nr");
		gameData.setLongitude(1.234f);
		gameData.setLatitude(5.678f);
		gameData.setRound_number(1);

		entityManager.persist(gameData);
		entityManager.flush();

		// when
		Game_data found = gameDataRepository.findByDataId(gameData.getDataId());

		// then
		assertEquals(found.getSession_id(), gameData.getSession_id());
	}

}
