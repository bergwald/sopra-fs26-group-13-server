package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private static final int MAX_BIO_LENGTH = 280;

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User getUserById(Long userId) {
		return this.userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						String.format("User with id %d was not found.", userId)));
	}

	public User createUser(User newUser, String rawPassword) {
		validateRegistrationInput(newUser, rawPassword);
		checkIfUsernameExists(newUser.getUsername());
		newUser.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
		newUser.setToken(UUID.randomUUID().toString());
		newUser.setStatus(UserStatus.ONLINE);
		// saves the given entity but data is only persisted in the database once
		// flush() is called
		newUser = userRepository.save(newUser);
		userRepository.flush();

		log.debug("Created Information for User: {}", newUser);
		return newUser;
	}

	public User loginUser(String username, String rawPassword) {
		if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username or password provided is incorrect.");
		}

		User user = userRepository.findByUsername(username.trim());
		if (user == null || !BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username or password provided is incorrect.");
		}

		user.setStatus(UserStatus.ONLINE);
		userRepository.save(user);
		userRepository.flush();
		return user;
	}

	public void logoutUser(String token) {
		if (token == null || token.isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid.");
		}

		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The provided token is invalid.");
		}

		user.setStatus(UserStatus.OFFLINE);
		user.setToken(UUID.randomUUID().toString());
		userRepository.save(user);
		userRepository.flush();
	}

	/**
	 * This helper validates required input for registration.
	 *
	 * @param userToBeCreated
	 * @param rawPassword
	 */
	private void validateRegistrationInput(User userToBeCreated, String rawPassword) {
		if (userToBeCreated == null || userToBeCreated.getUsername() == null || userToBeCreated.getUsername().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The username must not be empty.");
		}

		if (userToBeCreated.getName() == null || userToBeCreated.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The name must not be empty.");
		}

		userToBeCreated.setName(userToBeCreated.getName().trim());
		if (userToBeCreated.getName().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The name must not be empty.");
		}

		userToBeCreated.setUsername(userToBeCreated.getUsername().trim());
		if (userToBeCreated.getUsername().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The username must not be empty.");
		}

		if (rawPassword == null || rawPassword.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password must not be empty.");
		}

		if (rawPassword.length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password must be at least 8 characters long.");
		}

		String normalizedBio = normalizeBio(userToBeCreated.getBio());
		if (normalizedBio.length() > MAX_BIO_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The bio must be at most %d characters long.", MAX_BIO_LENGTH));
		}
		userToBeCreated.setBio(normalizedBio);
	}

	private String normalizeBio(String bio) {
		if (bio == null) {
			return "";
		}
		return bio.trim();
	}

	/**
	 * This helper checks the username uniqueness criteria.
	 *
	 * @param username
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUsernameExists(String username) {
		User existingUser = userRepository.findByUsername(username);
		if (existingUser != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "The username provided is not unique.");
		}
	}
}
