package ch.uzh.ifi.hase.soprafs26.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class WikidataMountainService {

    private static final String MOUNTAIN_QUERY_TEMPLATE = """
            SELECT ?mountain ?mountainLabel ?image ?location WHERE {
              SERVICE wikibase:box {
                ?mountain wdt:P625 ?location .
                bd:serviceParam wikibase:cornerWest "Point(%.6f %.6f)"^^geo:wktLiteral .
                bd:serviceParam wikibase:cornerEast "Point(%.6f %.6f)"^^geo:wktLiteral .
              }
              ?mountain wdt:P31 wd:Q8502;
                        wdt:P18 ?image.
              SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
            }
            LIMIT 20
            """;

    private static final List<SearchRegion> DEFAULT_SEARCH_REGIONS = List.of(
            new SearchRegion("Alps", 5.5, 44.0, 16.5, 48.8),
            new SearchRegion("Caucasus", 37.0, 39.5, 50.5, 44.8),
            new SearchRegion("Atlas", -9.5, 28.0, 5.0, 37.5),
            new SearchRegion("Himalayas", 72.0, 26.0, 98.0, 37.5),
            new SearchRegion("Japanese Alps", 136.0, 35.0, 139.5, 37.8),
            new SearchRegion("Rockies", -126.0, 37.0, -105.0, 55.0),
            new SearchRegion("Central Andes", -75.0, -38.0, -64.0, -10.0),
            new SearchRegion("Southern Alps", 167.0, -46.5, 171.8, -42.0));

    private static final Pattern POINT_PATTERN = Pattern.compile("Point\\(([-+0-9.]+) ([-+0-9.]+)\\)");

    private final WikidataClient wikidataClient;
    private final ObjectMapper objectMapper;
    private final Random random;
    private final List<SearchRegion> searchRegions;

    @Autowired
    public WikidataMountainService(WikidataClient wikidataClient) {
        this(wikidataClient, new Random(), DEFAULT_SEARCH_REGIONS);
    }

    WikidataMountainService(WikidataClient wikidataClient, Random random, List<SearchRegion> searchRegions) {
        this.wikidataClient = wikidataClient;
        this.objectMapper = new ObjectMapper();
        this.random = random;
        this.searchRegions = List.copyOf(searchRegions);
    }

    public WikidataMountainCandidate fetchMountainCandidate() {
        if (searchRegions.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No mountain search regions are configured.");
        }

        int startIndex = random.nextInt(searchRegions.size());

        for (int offset = 0; offset < searchRegions.size(); offset++) {
            SearchRegion region = searchRegions.get((startIndex + offset) % searchRegions.size());
            List<WikidataMountainCandidate> candidates = fetchCandidatesForRegion(region);
            if (!candidates.isEmpty()) {
                return candidates.get(random.nextInt(candidates.size()));
            }
        }

        throw new ResponseStatusException(NOT_FOUND, "No mountain with image and coordinates found on Wikidata.");
    }

    private List<WikidataMountainCandidate> fetchCandidatesForRegion(SearchRegion region) {
        String query = buildMountainQuery(region);
        JsonNode response = parseResponse(wikidataClient.executeSelectQuery(query));
        JsonNode bindings = response.path("results").path("bindings");
        List<WikidataMountainCandidate> candidates = new ArrayList<>();

        if (!bindings.isArray()) {
            return candidates;
        }

        for (JsonNode binding : bindings) {
            WikidataMountainCandidate candidate = toCandidate(binding);
            if (candidate != null) {
                candidates.add(candidate);
            }
        }

        return candidates;
    }

    private String buildMountainQuery(SearchRegion region) {
        return String.format(
                Locale.ROOT,
                MOUNTAIN_QUERY_TEMPLATE,
                region.cornerWestLongitude(),
                region.cornerWestLatitude(),
                region.cornerEastLongitude(),
                region.cornerEastLatitude());
    }

    private JsonNode parseResponse(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Received invalid JSON from Wikidata.", exception);
        }
    }

    private WikidataMountainCandidate toCandidate(JsonNode binding) {
        String entityUrl = readBindingValue(binding, "mountain");
        String imageUrl = readBindingValue(binding, "image");
        String location = readBindingValue(binding, "location");

        if (entityUrl == null || imageUrl == null || location == null) {
            return null;
        }

        Matcher matcher = POINT_PATTERN.matcher(location);
        if (!matcher.matches()) {
            return null;
        }

        try {
            double longitude = Double.parseDouble(matcher.group(1));
            double latitude = Double.parseDouble(matcher.group(2));
            String entityId = entityUrl.substring(entityUrl.lastIndexOf('/') + 1);
            String mountainName = readBindingValue(binding, "mountainLabel");

            if (mountainName == null || mountainName.isBlank()) {
                mountainName = entityId;
            }

            return new WikidataMountainCandidate(entityId, mountainName, imageUrl, latitude, longitude);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Received invalid coordinate data from Wikidata.",
                    exception);
        }
    }

    private String readBindingValue(JsonNode binding, String name) {
        JsonNode valueNode = binding.path(name).path("value");
        if (valueNode.isMissingNode() || valueNode.asText().isBlank()) {
            return null;
        }
        return valueNode.asText();
    }

    record SearchRegion(
            String name,
            double cornerWestLongitude,
            double cornerWestLatitude,
            double cornerEastLongitude,
            double cornerEastLatitude) {
    }
}
