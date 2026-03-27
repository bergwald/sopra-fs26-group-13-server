package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDateTime;

public class SessionGetDTO {

    private String id;
    private LocalDateTime sessionExpiryDateTime;
    private Integer roundNumber;

    public String getId() {
        return this.id;
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

}