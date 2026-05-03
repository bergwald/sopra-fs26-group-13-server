package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class UserAnswerPutDTO {

	private double latitude;
	private double longitude;
	private double distance;
	private int scoreRound;
	private long scoreOverall;
	private double guessLatitude;
	private double guessLongitude;

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getScoreRound() {
		return scoreRound;
	}

	public void setScoreRound(int scoreRound) {
		this.scoreRound = scoreRound;
	}

	public long getScoreOverall() {
		return scoreOverall;
	}

	public void setScoreOverall(long scoreOverall) {
		this.scoreOverall = scoreOverall;
	}

	public double getGuessLatitude() {
		return guessLatitude;
	}

	public void setGuessLatitude(double guessLatitude) {
		this.guessLatitude = guessLatitude;
	}

	public double getGuessLongitude() {
		return guessLongitude;
	}

	public void setGuessLongitude(double guessLongitude) {
		this.guessLongitude = guessLongitude;
	}
}
