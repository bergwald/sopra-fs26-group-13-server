package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.GameDataGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.GameService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class GameController {

	private final GameService gameService;

	GameController(GameService gameService) {
		this.gameService = gameService;
	}

	@GetMapping("/game_data")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public GameDataGetDTO GameDataGetDTO (@RequestBody GameGetDTO gameGetDTOin) {
		Game_data input = DTOMapper.INSTANCE.convertGameGetDTOToEntity(gameGetDTOin);
		Game_data output = gameService.getGameData(input);
		return DTOMapper.INSTANCE.convertEntityToGameDataGetDTO(output);
	}
}
