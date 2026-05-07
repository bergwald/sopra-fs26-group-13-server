package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.Instant;

public class UserProfileGetDTO {

	private Long id;
	private String username;
	private String bio;
	private Integer mascot_id;
	private Integer rounds_played;
	private Double avg_distance;
	private Double avg_score;
	private Instant creationDate;

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

	public Integer getRounds_played() {
		return rounds_played;
	}

	public void setRounds_played(Integer rounds_played) {
		this.rounds_played = rounds_played;
	}

	public Double getAvg_distance() {
		return avg_distance;
	}

	public void setAvg_distance(Double avg_distance) {
		this.avg_distance = avg_distance;
	}

	public Double getAvg_score() {
		return avg_score;
	}

	public void setAvg_score(Double avg_score) {
		this.avg_score = avg_score;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}
}
