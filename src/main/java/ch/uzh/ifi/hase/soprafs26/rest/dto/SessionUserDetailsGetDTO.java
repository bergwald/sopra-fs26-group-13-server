package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

import ch.uzh.ifi.hase.soprafs26.constant.UserSessionRole;

public class SessionUserDetailsGetDTO {

    private Long id;
    private String sessionId;
    private LocalDateTime sessionExpiryDateTime;
    private Integer roundNumber;
    private Long score;
    private UserSessionRole userRole;
    private LocalDateTime roundStartedDateTime;

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

    public LocalDateTime getSessionExpiryDateTime() {
        return sessionExpiryDateTime;
    }

    public void setSessionExpiryDateTime(LocalDateTime sessionExpiryDateTime) {
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

    public LocalDateTime getRoundStartedDateTime() {
        return roundStartedDateTime;
    }

    public void setRoundStartedDateTime(LocalDateTime roundStartedDateTime) {
        this.roundStartedDateTime = roundStartedDateTime;
    }

}