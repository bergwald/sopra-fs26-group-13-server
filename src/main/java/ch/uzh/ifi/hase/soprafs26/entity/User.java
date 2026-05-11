package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;

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
@Table(name = "users")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false, unique = true, updatable = false)
	private String username;

	@Column(nullable = false, length = 280)
	private String bio;

	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false, unique = true)
	private String token;

	@Column(nullable = false)
	private Integer mascotId = 1;

	@Column(nullable = false)
	private Integer roundsPlayed = 0;

	@Column(nullable = false)
	private Double avgDistance = 0.0;

	@Column(nullable = false)
	private Long score = 0L;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
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

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Integer getMascotId() {
		return mascotId;
	}

	public void setMascotId(Integer mascotId) {
		this.mascotId = mascotId;
	}

	public Integer getRoundsPlayed() {
		return roundsPlayed;
	}

	public void setRoundsPlayed(Integer roundsPlayed) {
		this.roundsPlayed = roundsPlayed;
	}

	public Double getAvgDistance() {
		return avgDistance;
	}

	public void setAvgDistance(Double avgDistance) {
		this.avgDistance = avgDistance;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}
}
