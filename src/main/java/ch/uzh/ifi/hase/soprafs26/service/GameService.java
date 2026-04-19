package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GameService {

    private final GameDataRepository gameDataRepository;

    public GameService(GameDataRepository gameDataRepository) {
        this.gameDataRepository = gameDataRepository;
    }
    public Game_data getSessionRoundData(Game_data gameData){
        Game_data foundData = gameDataRepository.findBySessionIdAndRoundNumber(gameData.getSessionId(), gameData.getRoundNumber());
        if (foundData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game data not found");
        }
        return foundData;
    }

}
