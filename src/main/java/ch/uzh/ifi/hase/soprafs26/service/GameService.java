package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Game_data;
import ch.uzh.ifi.hase.soprafs26.entity.Session;
import ch.uzh.ifi.hase.soprafs26.entity.SessionUser;
import ch.uzh.ifi.hase.soprafs26.repository.GameDataRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SessionUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Service
@Transactional
public class GameService {
    private static final int SINGLEPLAYER_TOTAL_ROUNDS = 3;

    private final GameDataRepository gameDataRepository;
    private final SessionUserRepository sessionUserRepository;
    private final SessionRepository sessionRepository;

    public GameService(GameDataRepository gameDataRepository,
            SessionUserRepository sessionUserRepository,
            SessionRepository sessionRepository) {
        this.gameDataRepository = gameDataRepository;
        this.sessionUserRepository = sessionUserRepository;
        this.sessionRepository = sessionRepository;
    }
    public Game_data getSessionRoundData(Game_data gameData){
        Game_data foundData = gameDataRepository.findBySessionIdAndRoundNumber(gameData.getSessionId(), gameData.getRoundNumber());
        if (foundData == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game data not found");
        }
        return foundData;
    }

    public Game_data getSessionRoundDataForUser(Long userId, String sessionId, int roundNumber) {
        UUID sessionUuid = parseSessionId(sessionId);
        requireSessionMembership(userId, sessionUuid);

        Game_data lookup = new Game_data();
        lookup.setSessionId(sessionId);
        lookup.setRoundNumber(roundNumber);
        return getSessionRoundData(lookup);
    }

    /**
     * Adds {@code roundScore} to the {@link SessionUser} score for the given user and session.
     */
    public long saveScore(Long userId, int roundScore, String sessionId) {
        UUID sessionUuid = parseSessionId(sessionId);
        SessionUser sessionUser = requireSessionMembership(userId, sessionUuid);
        long current = sessionUser.getScore() != null ? sessionUser.getScore() : 0;
        sessionUser.setScore(current + roundScore);
        sessionUserRepository.save(sessionUser);
        return sessionUser.getScore();
    }

    public int advanceSinglePlayerRound(String sessionId, int submittedRoundNumber) {
        UUID sessionUuid = parseSessionId(sessionId);
        Session session = sessionRepository.findById(sessionUuid);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found");
        }
        if (session.getRoundNumber() == null || session.getRoundNumber() != submittedRoundNumber) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session round is out of sync");
        }
        if (submittedRoundNumber < 1 || submittedRoundNumber > SINGLEPLAYER_TOTAL_ROUNDS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid round number");
        }

        int nextRoundNumber = submittedRoundNumber < SINGLEPLAYER_TOTAL_ROUNDS
                ? submittedRoundNumber + 1
                : SINGLEPLAYER_TOTAL_ROUNDS + 1;
        session.setRoundNumber(nextRoundNumber);
        sessionRepository.save(session);
        return nextRoundNumber;
    }

    private SessionUser requireSessionMembership(Long userId, UUID sessionUuid) {
        return sessionUserRepository.findByUserIdAndSessionId(userId, sessionUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session user not found"));
    }

    private static UUID parseSessionId(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid session id");
        }
    }

}
