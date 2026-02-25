package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserProfileGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserRegisterResponseDTO;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		userPostDTO.setName("name");
		userPostDTO.setUsername("username");
		userPostDTO.setPassword("password123");
		userPostDTO.setBio("short bio");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getName(), user.getName());
		assertEquals(userPostDTO.getUsername(), user.getUsername());
		assertEquals(userPostDTO.getBio(), user.getBio());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getName(), userGetDTO.getName());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getBio(), userGetDTO.getBio());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testRegisterResponse_fromUser_toUserRegisterResponseDTO_success() {
		User user = new User();
		user.setId(1L);
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("token-123");

		UserRegisterResponseDTO registerResponseDTO = DTOMapper.INSTANCE.convertEntityToUserRegisterResponseDTO(user);

		assertEquals(user.getId(), registerResponseDTO.getId());
		assertEquals(user.getName(), registerResponseDTO.getName());
		assertEquals(user.getUsername(), registerResponseDTO.getUsername());
		assertEquals(user.getBio(), registerResponseDTO.getBio());
		assertEquals(user.getStatus(), registerResponseDTO.getStatus());
		assertEquals(user.getToken(), registerResponseDTO.getToken());
	}

	@Test
	public void testGetUserProfile_fromUser_toUserProfileGetDTO_success() {
		User user = new User();
		user.setId(1L);
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setBio("Hello from bio");
		user.setStatus(UserStatus.ONLINE);
		user.setCreationDate(Instant.parse("2026-02-25T14:35:00Z"));

		UserProfileGetDTO userProfileGetDTO = DTOMapper.INSTANCE.convertEntityToUserProfileGetDTO(user);

		assertEquals(user.getId(), userProfileGetDTO.getId());
		assertEquals(user.getName(), userProfileGetDTO.getName());
		assertEquals(user.getUsername(), userProfileGetDTO.getUsername());
		assertEquals(user.getBio(), userProfileGetDTO.getBio());
		assertEquals(user.getStatus(), userProfileGetDTO.getStatus());
		assertEquals(user.getCreationDate(), userProfileGetDTO.getCreationDate());
	}
}
