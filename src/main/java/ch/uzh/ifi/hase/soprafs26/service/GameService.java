package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GameService {

    private final GameDataRepository gameDataRepository;

    public GameService(GameDataRepository gameDataRepository) {
        this.gameDataRepository = gameDataRepository;
    }
    public Game_data getGameData(Game_data gameData){
        return gameDataRepository.findBySessionIdAndRoundNumber(gameData.getSessionId(), gameData.getRoundNumber());
        //TODO: or else throw not found
    }
}
