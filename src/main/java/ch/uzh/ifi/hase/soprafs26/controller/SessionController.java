package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SessionUserDetailsGetDTO;

import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.SessionService;

import java.util.List;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;

/**
 * Session Controller
 * This class is responsible for handling all REST request that are related to
 * the session.
 * The controller will receive the request and delegate the execution to the
 * SessionService and finally return the result.
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
	public List<SessionGetDTO> getAllUsers(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {
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

	@GetMapping("/session/{sessionId}")
	@ResponseStatus(HttpStatus.OK)
	public List<SessionUserDetailsGetDTO> getSessionById(
			@PathVariable String sessionId,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {

		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);

		List<SessionUser> sessionUser = sessionService.getAllSessionUserForAuthorizedUser(UUID.fromString(sessionId),
				userId);

		// Convert to DTOs
		List<SessionUserDetailsGetDTO> sessionUserDTOs = new ArrayList<>();
		for (SessionUser su : sessionUser) {
			SessionUserDetailsGetDTO sessionUserDetail = new SessionUserDetailsGetDTO();
			sessionUserDetail.setId(su.getUser().getId());
			sessionUserDetail.setSessionId(su.getSession().getIdAsString());
			sessionUserDetail.setRoundNumber(su.getSession().getRoundNumber());
			sessionUserDetail.setSessionExpiryDateTime(su.getSession().getSessionExpiryDateTime());
			sessionUserDetail.setScore(su.getScore());
			sessionUserDetail.setUserRole(su.getUserRole());
			sessionUserDetail.setRoundStartedDateTime(su.getSession().getRoundStartedDateTime());
			sessionUserDetail.setGuessLatitude(su.getGuessLatitude());
			sessionUserDetail.setGuessLongitude(su.getGuessLongitude());
			sessionUserDTOs.add(sessionUserDetail);
		}
		return sessionUserDTOs;
	}

	@PutMapping("/session/{sessionId}/increaseRoundNumber")
	@ResponseStatus(HttpStatus.OK)
	public Integer increaseSessionRoundNumber(
			@PathVariable String sessionId,
			@RequestParam int currentRoundNumber,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {

		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		boolean userIsOwner = sessionService.checkIfUserIsSessionOwner(userId, sessionId);
		if (userIsOwner) {
			Session session = sessionService.getSessionWithId(sessionId);
			Integer increasedRoundNumber = sessionService.increaseSessionRoundNumber(session, currentRoundNumber, 3);
			sessionService.resetCoordinatesOfSessionUsers(session.getId());
			return increasedRoundNumber;
		} else {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this session");
		}
	}

	@PostMapping("/session")
	@ResponseStatus(HttpStatus.CREATED)
	public SessionGetDTO createSession(@RequestBody SessionPostDTO sessionPost,
			@RequestParam(required = false, defaultValue = "") String region,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {
		// Creates a new session and returns the new session type
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		validateBodyUserId(userId, sessionPost.getUserId());

		Session createdSession = sessionService.createNewSession(sessionPost.getUserId());
		sessionService.initializeGameDate(createdSession, region);
		sessionService.userJoinSession(sessionPost.getUserId(), createdSession.getId(), UserSessionRole.OWNER);

		return DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(createdSession);
	}

	@PutMapping("/session")
	@ResponseStatus(HttpStatus.OK)
	public SessionGetDTO userJoinSession(@RequestBody SessionPutDTO sessionPut,
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestHeader(value = "userId", required = false) Long userId) {
		String token = this.userService.extractBearerToken(authorizationHeader);
		this.userService.getAuthorizedTargetUser(userId, token);
		validateBodyUserId(userId, sessionPut.getUserId());
		Session createdSession = sessionService.userJoinSession(sessionPut.getUserId(),
				UUID.fromString(sessionPut.getSessionId()), UserSessionRole.PLAYER);
		return DTOMapper.INSTANCE.convertEntitityToSessionGetDTO(createdSession);
	}

	private void validateBodyUserId(Long authenticatedUserId, Long bodyUserId) {
		if (authenticatedUserId == null || bodyUserId == null || !authenticatedUserId.equals(bodyUserId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id mismatch");
		}
	}

}
