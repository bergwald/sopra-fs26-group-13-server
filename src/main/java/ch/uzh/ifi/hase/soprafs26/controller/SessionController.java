package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;

import java.util.List;
import java.util.UUID;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */

@RestController
public class SessionController {

	private final UserService userService;
	private final SessionService sessionService;

	SessionController(SessionService sessionService, UserService userService) {
		this.userService = userService;
		this.sessionService = sessionService;
	}

	@GetMapping("/session")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<SessionGetDTO> getAllUsers(
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
		@RequestHeader(value = "userId", required = false) Long userId
	) {
		// fetch all sessions in the internal representation
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		List<Session> sessions = sessionService.getAllSessions();
		List<SessionGetDTO> sessionGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (Session session : sessions) {
			sessionGetDTOs.add(DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(session));
		}
		return sessionGetDTOs;
	}

	@PostMapping("/session")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SessionGetDTO createSession(@RequestBody SessionPostDTO sessionPost,
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
		@RequestHeader(value = "userId", required = false) Long userId
	) {
		// Creates a new session and returns the new session type
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		Session createdSession = sessionService.createNewSession();
		sessionService.userJoinSession(sessionPost.getUserId(), createdSession.getId());
		return DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(createdSession);
	}

	@PutMapping("/session")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public SessionGetDTO userJoinSession(@RequestBody SessionPutDTO sessionPut,
		@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
		@RequestHeader(value = "userId", required = false) Long userId
	) {
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		Session createdSession = sessionService.userJoinSession(sessionPut.getUserId(),
				UUID.fromString(sessionPut.getSessionId()));
		return DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(createdSession);
	}

}
