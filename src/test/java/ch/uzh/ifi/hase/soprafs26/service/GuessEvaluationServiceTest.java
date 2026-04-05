package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuessEvaluationServiceTest {

    private GuessEvaluationService guessEvaluationService;

    @BeforeEach
    public void setup() {
        guessEvaluationService = new GuessEvaluationService();
    }

    @Test
    public void computeDistanceKm_sameCoordinates_returnsZero() {
        double distance = guessEvaluationService.computeDistanceKm(47.3769, 8.5417, 47.3769, 8.5417);

        assertEquals(0.0, distance, 1e-9);
    }

    @Test
    public void computeDistanceKm_swappedCoordinates_returnsSameDistance() {
        double forwardDistance = guessEvaluationService.computeDistanceKm(47.3769, 8.5417, 46.9480, 7.4474);
        double reverseDistance = guessEvaluationService.computeDistanceKm(46.9480, 7.4474, 47.3769, 8.5417);

        assertEquals(forwardDistance, reverseDistance, 1e-9);
    }

    @Test
    public void computeDistanceKm_knownCoordinatePair_returnsExpectedApproximation() {
        double distance = guessEvaluationService.computeDistanceKm(0.0, 0.0, 0.0, 1.0);

        assertEquals(111.19, distance, 0.5);
    }

    @Test
    public void computeScore_zeroDistance_returnsMaximumScore() {
        int score = guessEvaluationService.computeScore(0.0);

        assertEquals(100, score);
    }

    @Test
    public void computeScore_halfScoreDistance_returnsFifty() {
        int score = guessEvaluationService.computeScore(1000.0);

        assertEquals(50, score);
    }

    @Test
    public void computeScore_doubleHalfScoreDistance_returnsTwentyFive() {
        int score = guessEvaluationService.computeScore(2000.0);

        assertEquals(25, score);
    }

    @Test
    public void computeScore_increasingDistance_decreasesMonotonically() {
        int nearScore = guessEvaluationService.computeScore(100.0);
        int mediumScore = guessEvaluationService.computeScore(1000.0);
        int farScore = guessEvaluationService.computeScore(5000.0);

        assertTrue(nearScore > mediumScore);
        assertTrue(mediumScore > farScore);
    }

    @Test
    public void computeScore_invalidDistance_returnsZero() {
        assertEquals(0, guessEvaluationService.computeScore(-1.0));
        assertEquals(0, guessEvaluationService.computeScore(Double.NaN));
        assertEquals(0, guessEvaluationService.computeScore(Double.POSITIVE_INFINITY));
    }
}
