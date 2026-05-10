package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionGetDTO {

    private String id;
    private String sessionExpiryDateTime;
    private Integer roundNumber;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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

}
