package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "game_data")
public class Game_data implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long data_id;

	@Column(nullable = false)
	private String session_id;

	@Column(nullable = true)
	private String image_url;

	@Column(nullable = true)
	private float longitude;

	@Column(nullable = true)
	private float latitude;

	@Column(nullable = true)
	private int round_number;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSession_id() {
		return session_id;
	}

	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public int getRound_number() {
		return round_number;
	}

	public void setRound_number(int round_number) {
		this.round_number = round_number;
	}
}
