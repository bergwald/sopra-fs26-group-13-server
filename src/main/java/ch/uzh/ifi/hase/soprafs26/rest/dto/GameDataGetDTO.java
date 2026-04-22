package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class GameDataGetDTO {

	private String imageUrl;
	private int roundNumber;
	private String sessionId;
	private LocalDateTime roundStartedDateTime;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public LocalDateTime getRoundStartedDateTime() {
		return roundStartedDateTime;
	}

	public void setRoundStartedDateTime(LocalDateTime roundStartedDateTime) {
		this.roundStartedDateTime = roundStartedDateTime;
	}
}
