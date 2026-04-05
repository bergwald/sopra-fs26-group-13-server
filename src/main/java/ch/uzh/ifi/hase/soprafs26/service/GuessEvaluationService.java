package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;

@Service
public class GuessEvaluationService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final int MAX_SCORE = 100;
    private static final double HALF_SCORE_DISTANCE_KM = 1000.0;

    public double computeDistanceKm(double guessLatitude, double guessLongitude, double actualLatitude,
            double actualLongitude) {
        double latitudeDeltaRadians = Math.toRadians(actualLatitude - guessLatitude);
        double longitudeDeltaRadians = Math.toRadians(actualLongitude - guessLongitude);
        double guessLatitudeRadians = Math.toRadians(guessLatitude);
        double actualLatitudeRadians = Math.toRadians(actualLatitude);

        double haversine = Math.sin(latitudeDeltaRadians / 2) * Math.sin(latitudeDeltaRadians / 2)
                + Math.cos(guessLatitudeRadians) * Math.cos(actualLatitudeRadians)
                        * Math.sin(longitudeDeltaRadians / 2) * Math.sin(longitudeDeltaRadians / 2);

        double angularDistance = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return EARTH_RADIUS_KM * angularDistance;
    }

    public int computeScore(double distanceKm) {
        if (!Double.isFinite(distanceKm) || distanceKm < 0) {
            return 0;
        }

        double score = MAX_SCORE * Math.pow(0.5, distanceKm / HALF_SCORE_DISTANCE_KM);
        long roundedScore = Math.round(score);

        if (roundedScore < 0) {
            return 0;
        }
        if (roundedScore > MAX_SCORE) {
            return MAX_SCORE;
        }

        return (int) roundedScore;
    }
}
