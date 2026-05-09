package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserRegisterResponseDTO {

	private Long id;
	private String username;
	private String bio;
	private Integer mascot_id;
	private String token;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public Integer getMascot_id() {
		return mascot_id;
	}

	public void setMascot_id(Integer mascot_id) {
		this.mascot_id = mascot_id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
