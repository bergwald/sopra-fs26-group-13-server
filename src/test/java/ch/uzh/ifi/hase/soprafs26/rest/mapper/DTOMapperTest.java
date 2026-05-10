package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAnswerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserRegisterResponseDTO;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {

	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setUsername("username");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("short bio");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getUsername(), user.getUsername());
		assertEquals(userPostDTO.getBio(), user.getBio());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setId(1L);
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setToken("1");
		user.setMascotId(3);
		user.setRoundsPlayed(4);
		user.setAvgDistance(1250.5);
		user.setAvgScore(72.25);

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getBio(), userGetDTO.getBio());
		assertEquals(user.getMascotId(), userGetDTO.getMascot_id());
		assertEquals(user.getRoundsPlayed(), userGetDTO.getRounds_played());
		assertEquals(user.getAvgDistance(), userGetDTO.getAvg_distance());
		assertEquals(user.getAvgScore(), userGetDTO.getAvg_score());
	}

	@Test
	public void testRegisterResponse_fromUser_toUserRegisterResponseDTO_success() {
		User user = new User();
		user.setId(1L);
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setToken("token-123");
		user.setMascotId(2);

		UserRegisterResponseDTO registerResponseDTO = DTOMapper.INSTANCE.convertEntityToUserRegisterResponseDTO(user);

		assertEquals(user.getId(), registerResponseDTO.getId());
		assertEquals(user.getUsername(), registerResponseDTO.getUsername());
		assertEquals(user.getBio(), registerResponseDTO.getBio());
		assertEquals(user.getMascotId(), registerResponseDTO.getMascot_id());
		assertEquals(user.getToken(), registerResponseDTO.getToken());
	}

	@Test
	public void testGetUserProfile_fromUser_toUserProfileGetDTO_success() {
		User user = new User();
		user.setId(1L);
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setMascotId(4);
		user.setRoundsPlayed(3);
		user.setAvgDistance(2000.0);
		user.setAvgScore(50.0);
		user.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		UserProfileGetDTO userProfileGetDTO = DTOMapper.INSTANCE.convertEntityToUserProfileGetDTO(user);

		assertEquals(user.getId(), userProfileGetDTO.getId());
		assertEquals(user.getUsername(), userProfileGetDTO.getUsername());
		assertEquals(user.getBio(), userProfileGetDTO.getBio());
		assertEquals(user.getMascotId(), userProfileGetDTO.getMascot_id());
		assertEquals(user.getRoundsPlayed(), userProfileGetDTO.getRounds_played());
		assertEquals(user.getAvgDistance(), userProfileGetDTO.getAvg_distance());
		assertEquals(user.getAvgScore(), userProfileGetDTO.getAvg_score());
		assertEquals(user.getCreationDate(), userProfileGetDTO.getCreationDate());
	}

	@Test
	public void test_getSessionRoundURL_from_GameData_to_GameDataGetDTO_success() {
		// create Game_data
		Game_data gameData = new Game_data();
		gameData.setDataId(Long.valueOf(123456));
		gameData.setSessionId("Session11111");
		gameData.setImageUrl("example.com/panorama-image");
		gameData.setLongitude(1.0f);
		gameData.setLatitude(4.0f);
		gameData.setRoundNumber(3);

		// MAP -> Create GameDataGetDTO
		GameDataGetDTO gameDataGetDTO = DTOMapper.INSTANCE.convertEntityToGameDataGetDTO(gameData);

		// check content
		assertEquals(gameData.getSessionId(), gameDataGetDTO.getSessionId());
		assertEquals(gameData.getImageUrl(), gameDataGetDTO.getImageUrl());
		assertEquals(gameData.getRoundNumber(), gameDataGetDTO.getRoundNumber());
	}

	@Test
	public void test_getSessionRoundURL_from_GameGetDTO_to_GameData_success() {
		// create GameGetDTO
		GameGetDTO gameGetDTO = new GameGetDTO();
		gameGetDTO.setSessionId("Session1234");
		gameGetDTO.setRoundNumber(2);

		// MAP -> Create Game_data
		Game_data gameData = DTOMapper.INSTANCE.convertGameGetDTOToEntity(gameGetDTO);

		// check content
		assertEquals(gameData.getSessionId(), gameGetDTO.getSessionId());
		assertEquals(gameData.getRoundNumber(), gameGetDTO.getRoundNumber());
	}

	@Test
	public void testGetSession_fromSession_toSessionGetDTO_success() {
		LocalDateTime currentDateTime = LocalDateTime.of(2026, 1, 1, 8, 30, 0);

		Session session = new Session();
		session.setRoundNumber(1);
		session.setSessionExpiryDateTime(currentDateTime);

		SessionGetDTO sessionGetDTO = DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(session);

		assertEquals(session.getIdAsString(), sessionGetDTO.getId());
		assertEquals(session.getRoundNumber(), sessionGetDTO.getRoundNumber());
		assertEquals(ApiDateTimeFormatter.toUtcIsoString(session.getSessionExpiryDateTime()),
				sessionGetDTO.getSessionExpiryDateTime());
	}

	@Test
	public void test_submitGuess_from_UserGuessPutDTO_to_Game_data_lookup_success() {
		UserGuessPutDTO guess = new UserGuessPutDTO();
		guess.setUserId(5L);
		guess.setSessionId("session-uuid-string");
		guess.setRoundNumber(4);
		guess.setLatitude(47.3);
		guess.setLongitude(8.5);

		Game_data lookup = DTOMapper.INSTANCE.convertUserGuessPutDTOToEntity(guess);

		assertEquals(guess.getSessionId(), lookup.getSessionId());
		assertEquals(4, lookup.getRoundNumber());
	}

	@Test
	public void test_submitGuess_from_Game_data_and_scores_to_UserAnswerPutDTO_success() {
		Game_data gameData = new Game_data();
		gameData.setLatitude(46.95d);
		gameData.setLongitude(7.44d);

		UserAnswerPutDTO answer = DTOMapper.INSTANCE.convertEntityToUserAnswerPutDTO(gameData, 12.5d, 88, 200L, 46.95d, 7.44d);

		assertEquals(gameData.getLatitude(), answer.getLatitude(), 1e-6);
		assertEquals(gameData.getLongitude(), answer.getLongitude(), 1e-6);
		assertEquals(12.5d, answer.getDistance(), 0d);
		assertEquals(88, answer.getScoreRound());
		assertEquals(200L, answer.getScoreOverall());
	}
}
