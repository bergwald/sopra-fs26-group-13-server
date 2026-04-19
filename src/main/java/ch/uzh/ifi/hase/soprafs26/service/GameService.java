package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Service
@Transactional
public class GameService {

    private final GameDataRepository gameDataRepository;
    private final SessionUserRepository sessionUserRepository;

    public GameService(GameDataRepository gameDataRepository,
            SessionUserRepository sessionUserRepository) {
        this.gameDataRepository = gameDataRepository;
        this.sessionUserRepository = sessionUserRepository;
    }
    public Game_data getSessionRoundData(Game_data gameData){
        Game_data foundData = gameDataRepository.findBySessionIdAndRoundNumber(gameData.getSessionId(), gameData.getRoundNumber());
        if (foundData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game data not found");
        }
        return foundData;
    }

    /**
     * Adds {@code roundScore} to the {@link SessionUser} score for the given user and session.
     */
    public long saveScore(Long userId, int roundScore, String sessionId) {
        UUID sessionUuid = parseSessionId(sessionId);
        SessionUser sessionUser = sessionUserRepository.findByUserIdAndSessionId(userId, sessionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session user not found"));
        long current = sessionUser.getScore() != null ? sessionUser.getScore() : 0;
        sessionUser.setScore(current + roundScore);
        sessionUserRepository.save(sessionUser);
        return sessionUser.getScore();
    }

    private static UUID parseSessionId(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid session id");
        }
    }

}
