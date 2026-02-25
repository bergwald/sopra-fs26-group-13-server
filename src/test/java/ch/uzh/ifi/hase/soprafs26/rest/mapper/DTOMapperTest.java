package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserRegisterResponseDTO;

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

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getName(), user.getName());
		assertEquals(userPostDTO.getUsername(), user.getUsername());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getName(), userGetDTO.getName());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testRegisterResponse_fromUser_toUserRegisterResponseDTO_success() {
		User user = new User();
		user.setId(1L);
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("token-123");

		UserRegisterResponseDTO registerResponseDTO = DTOMapper.INSTANCE.convertEntityToUserRegisterResponseDTO(user);

		assertEquals(user.getId(), registerResponseDTO.getId());
		assertEquals(user.getName(), registerResponseDTO.getName());
		assertEquals(user.getUsername(), registerResponseDTO.getUsername());
		assertEquals(user.getStatus(), registerResponseDTO.getStatus());
		assertEquals(user.getToken(), registerResponseDTO.getToken());
	}
}
