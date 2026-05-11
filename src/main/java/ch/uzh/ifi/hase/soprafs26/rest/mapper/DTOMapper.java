package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;

import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserRegisterResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAnswerPutDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "passwordHash", ignore = true)
	@Mapping(target = "token", ignore = true)
	@Mapping(target = "mascotId", ignore = true)
	@Mapping(target = "roundsPlayed", ignore = true)
	@Mapping(target = "avgDistance", ignore = true)
	@Mapping(target = "score", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "mascotId", target = "mascot_id")
	@Mapping(source = "roundsPlayed", target = "rounds_played")
	@Mapping(source = "avgDistance", target = "avg_distance")
	@Mapping(source = "score", target = "score")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "mascotId", target = "mascot_id")
	@Mapping(source = "roundsPlayed", target = "rounds_played")
	@Mapping(source = "avgDistance", target = "avg_distance")
	@Mapping(source = "score", target = "score")
	@Mapping(source = "creationDate", target = "creationDate")
	UserProfileGetDTO convertEntityToUserProfileGetDTO(User user);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "mascotId", target = "mascot_id")
	@Mapping(source = "token", target = "token")
	UserRegisterResponseDTO convertEntityToUserRegisterResponseDTO(User user);

	@Mapping(source = "sessionId", target = "sessionId")
	@Mapping(source = "imageUrl", target = "imageUrl")
	@Mapping(source = "roundNumber", target = "roundNumber")
	@Mapping(target = "roundStartedDateTime", expression = "java(null)")
	GameDataGetDTO convertEntityToGameDataGetDTO(Game_data game_data);

	@Mapping(source = "sessionId", target = "sessionId")
	@Mapping(source = "roundNumber", target = "roundNumber")
	Game_data convertGameGetDTOToEntity(GameGetDTO gameGetDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(target = "sessionExpiryDateTime", expression = "java(ApiDateTimeFormatter.toUtcIsoString(session.getSessionExpiryDateTime()))")
	@Mapping(source = "roundNumber", target = "roundNumber")
	SessionGetDTO convertEntitityToSessionGetDTO(Session session);

	@Mapping(source = "sessionId", target = "sessionId")
	@Mapping(source = "roundNumber", target = "roundNumber")
	@Mapping(target = "dataId", ignore = true)
	@Mapping(target = "imageUrl", ignore = true)
	@Mapping(target = "longitude", ignore = true)
	@Mapping(target = "latitude", ignore = true)
	Game_data convertUserGuessPutDTOToEntity(UserGuessPutDTO userGuess);

	@Mapping(source = "gameData.latitude", target = "latitude")
	@Mapping(source = "gameData.longitude", target = "longitude")
	@Mapping(source = "distance", target = "distance")
	@Mapping(source = "scoreRound", target = "scoreRound")
	@Mapping(source = "scoreOverall", target = "scoreOverall")
	UserAnswerPutDTO convertEntityToUserAnswerPutDTO(Game_data gameData, double distance, int scoreRound,
			long scoreOverall, double guessLatitude, double guessLongitude);

}
