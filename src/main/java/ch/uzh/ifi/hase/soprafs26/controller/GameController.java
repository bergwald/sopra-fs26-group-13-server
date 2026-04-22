package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAnswerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.GuessEvaluationService;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class GameController {

	private final UserService userService;
	private final GameService gameService;
	private final SessionService sessionService;
	private final GuessEvaluationService guessEvaluationService;

	GameController(GameService gameService, UserService userService,
			GuessEvaluationService guessEvaluationService,
			SessionService sessionService) {
		this.gameService = gameService;
		this.userService = userService;
		this.guessEvaluationService = guessEvaluationService;
		this.sessionService = sessionService;
	}

	@GetMapping("/game_data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public GameDataGetDTO getSessionRoundURL(@RequestParam String sessionId,
			@RequestParam int roundNumber,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {

		// authorize user logged in
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);

		Game_data output = gameService.getSessionRoundDataForUser(userId, sessionId, roundNumber);
		Session session = sessionService.getSessionWithId(sessionId);
		GameDataGetDTO gameData = DTOMapper.INSTANCE.convertEntityToGameDataGetDTO(output);
		gameData.setRoundStartedDateTime(session.getRoundStartedDateTime());
		return gameData;
	}

	@PutMapping("/submit_guess")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserAnswerPutDTO makeGuess(@RequestBody UserGuessPutDTO userGuessObj,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {

		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);

		if (userGuessObj.getUserId() != null && !userGuessObj.getUserId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id mismatch");
		}
		if (userGuessObj.getRoundNumber() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Round number is required");
		}

		Game_data gameData = gameService.getSessionRoundDataForUser(
				userGuessObj.getUserId(),
				userGuessObj.getSessionId(),
				userGuessObj.getRoundNumber());

		double distance;
		int scoreRound;

		if (userGuessObj.getLatitude() == -1.0 && userGuessObj.getLatitude() == -1.0) {
			distance = -1.0;
			scoreRound = 0;

		} else {
			distance = guessEvaluationService.computeDistanceKm(userGuessObj.getLatitude(),
					userGuessObj.getLongitude(), gameData.getLatitude(), gameData.getLongitude());
			scoreRound = guessEvaluationService.computeScore(distance);
		}

		long scoreOverall = gameService.saveScore(userGuessObj.getUserId(), scoreRound, userGuessObj.getSessionId());
		gameService.validateSessionGameGuess(userGuessObj.getSessionId(), userGuessObj.getRoundNumber());

		return DTOMapper.INSTANCE.convertEntityToUserAnswerPutDTO(gameData, distance, scoreRound, scoreOverall);
	}
}
