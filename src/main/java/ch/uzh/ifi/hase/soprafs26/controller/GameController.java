package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGuessPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAnswerPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;
import ch.uzh.ifi.hase.soprafs26.service.GuessEvaluationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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
	private final GuessEvaluationService guessEvaluationService;
	private final SessionRepository sessionRepository;

	GameController(GameService gameService, UserService userService,
			GuessEvaluationService guessEvaluationService, SessionRepository sessionRepository) {
		this.gameService = gameService;
		this.userService = userService;
		this.guessEvaluationService = guessEvaluationService;
		this.sessionRepository = sessionRepository;
	}

	@GetMapping("/game_data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public GameDataGetDTO getSessionRoundURL(@RequestBody GameGetDTO gameGetDTOin,
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
		@RequestHeader(value = "userId", required = false) Long userId) {
		
		//authorize user logged in
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);

		//find game_data info and return propper obj
		Game_data input = DTOMapper.INSTANCE.convertGameGetDTOToEntity(gameGetDTOin);
		Game_data output = gameService.getSessionRoundData(input);
		return DTOMapper.INSTANCE.convertEntityToGameDataGetDTO(output);
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

		Game_data lookup = DTOMapper.INSTANCE.convertUserGuessPutDTOToEntity(userGuessObj);
		Game_data gameData = gameService.getSessionRoundData(lookup);

		double distance = guessEvaluationService.computeDistanceKm(userGuessObj.getLatitude(),
				userGuessObj.getLongitude(), gameData.getLatitude(), gameData.getLongitude());
		int scoreRound = guessEvaluationService.computeScore(distance);

		
		long scoreOverall = gameService.saveScore(userGuessObj.getUserId(), scoreRound, userGuessObj.getSessionId());

		return DTOMapper.INSTANCE.convertEntityToUserAnswerPutDTO(gameData, distance, scoreRound, scoreOverall);
	}
}
