package ch.uzh.ifi.hase.soprafs26.constant;

public record SearchRegion(
    String name,
    double minLongitude,
    double minLatitude,
    double maxLongitude,
    double maxLatitude) {
}
