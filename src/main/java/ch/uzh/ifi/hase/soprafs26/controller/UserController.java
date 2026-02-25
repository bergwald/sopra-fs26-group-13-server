package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserRegisterResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@GetMapping("/users/{userId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserProfileGetDTO getUserById(@PathVariable("userId") Long userId) {
		User user = userService.getUserById(userId);
		return DTOMapper.INSTANCE.convertEntityToUserProfileGetDTO(user);
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserRegisterResponseDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput, userPostDTO.getPassword());
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserRegisterResponseDTO(createdUser);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserRegisterResponseDTO loginUser(@RequestBody UserLoginDTO userLoginDTO) {
		User loggedInUser = userService.loginUser(userLoginDTO.getUsername(), userLoginDTO.getPassword());
		return DTOMapper.INSTANCE.convertEntityToUserRegisterResponseDTO(loggedInUser);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logoutUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
		String token = extractBearerToken(authorizationHeader);
		userService.logoutUser(token);
	}

	private String extractBearerToken(String authorizationHeader) {
		String bearerPrefix = "Bearer ";
		if (authorizationHeader == null || authorizationHeader.isBlank() || !authorizationHeader.startsWith(bearerPrefix)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid.");
		}

		String token = authorizationHeader.substring(bearerPrefix.length()).trim();
		if (token.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid.");
		}
		return token;
	}
}
