package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SessionPutDTO {
    private Long userId;
    private String sessionId;

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
