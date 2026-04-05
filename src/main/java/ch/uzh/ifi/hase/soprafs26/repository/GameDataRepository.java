package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;

@Repository("GameDataRepository")
public interface GameDataRepository extends JpaRepository<Game_data, Long> {
	Game_data findByDataId(long dataId);
}
