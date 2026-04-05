package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.stereotype.Service;

@Service
public class GuessEvaluationService {

    private static final double EARTH_RADIUS_KM = 6371.0;

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
}
