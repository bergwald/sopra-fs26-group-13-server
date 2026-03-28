package ch.uzh.ifi.hase.soprafs26.rest.dto;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

public class GameDataGetDTO {

	private String wikidata_url;
	private int round_number;
	private String session_id;

	public String getWikidata_url() {
		return wikidata_url;
	}

	public void setWikidata_url(String wikidata_url) {
		this.wikidata_url = wikidata_url;
	}

	public int getRound_number() {
		return round_number;
	}

	public void setRound_number(int round_number) {
		this.round_number = round_number;
	}

	public String getSession_id() {
		return session_id;
	}

	public void setSession_id(String session_id) {
		this.session_id = session_id;
	}
}
