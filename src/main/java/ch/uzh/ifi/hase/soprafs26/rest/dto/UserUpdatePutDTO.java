package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserUpdatePutDTO {

	private String bio;

	private String newPassword;

	private Integer mascot_id;

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public Integer getMascot_id() {
		return mascot_id;
	}

	public void setMascot_id(Integer mascot_id) {
		this.mascot_id = mascot_id;
	}
}
