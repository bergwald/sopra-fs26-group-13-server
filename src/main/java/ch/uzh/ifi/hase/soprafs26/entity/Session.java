package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "session")
public class Session implements Serializable {

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
