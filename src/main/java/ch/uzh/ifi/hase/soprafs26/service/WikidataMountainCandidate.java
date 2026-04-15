package ch.uzh.ifi.hase.soprafs26.service;

public record WikidataMountainCandidate(
        String wikidataEntityId,
        String mountainName,
        String imageUrl,
        double latitude,
        double longitude) {
}
