package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserUpdatePutDTO {

	private String bio;

	private String newPassword;

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
}
