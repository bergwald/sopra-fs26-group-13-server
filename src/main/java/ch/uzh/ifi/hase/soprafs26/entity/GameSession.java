package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal Session Representation
 * Holds the information about the GameSession
 */
@Entity
@Table(name = "session")
public class GameSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime sessionExpiryDateTime;

    @Column(nullable = false)
    private Integer roundNumber;

    public UUID getId() {
        return id;
    }

    public String getIdAsString() {
        return id == null ? null : id.toString();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getSessionExpiryDateTime() {
        return sessionExpiryDateTime;
    }

    public void setSessionExpiryDateTime(LocalDateTime sessionExpiryDateTime) {
        this.sessionExpiryDateTime = sessionExpiryDateTime;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }
}
