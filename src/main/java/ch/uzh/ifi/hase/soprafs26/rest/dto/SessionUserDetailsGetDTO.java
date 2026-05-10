package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;

public class SessionUserDetailsGetDTO {

    private Long id;
    private String sessionId;
    private String username;
    private String sessionExpiryDateTime;
    private Integer roundNumber;
    private Long score;
    private UserSessionRole userRole;
    private String roundStartedDateTime;
    private double guessLatitude;
    private double guessLongitude;
    private boolean guessSubmitted;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String id) {
        this.sessionId = id;
    }
    
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionExpiryDateTime() {
        return sessionExpiryDateTime;
    }

    public void setSessionExpiryDateTime(String sessionExpiryDateTime) {
        this.sessionExpiryDateTime = sessionExpiryDateTime;
    }

    public Integer getRoundNumber() {
        return this.roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public UserSessionRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserSessionRole userRole) {
        this.userRole = userRole;
    }

    public String getRoundStartedDateTime() {
        return roundStartedDateTime;
    }

    public void setRoundStartedDateTime(String roundStartedDateTime) {
        this.roundStartedDateTime = roundStartedDateTime;
    }

    public double getGuessLatitude() {
        return this.guessLatitude;
    }

    public void setGuessLatitude(double guessLatitude) {
        this.guessLatitude = guessLatitude;
    }

    public double getGuessLongitude() {
        return this.guessLongitude;
    }

    public void setGuessLongitude(double guessLongitude) {
        this.guessLongitude = guessLongitude;
    }

    public boolean getGuessSubmitted() {
        return this.guessSubmitted;
    }

    public void setGuessSubmitted(boolean guessSubmitted) {
        this.guessSubmitted = guessSubmitted;
    }

}
