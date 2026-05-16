package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.GameRegions;
import ch.uzh.ifi.hase.soprafs26.constant.SearchRegion;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class GooglePanoramaService {

    private final GoogleMapsHttpClient googleMapsHttpClient;
    private final GoogleMapsApiKeyProvider googleMapsApiKeyProvider;
    private final ObjectMapper objectMapper;
    private final Random random;
    private final String elevationEndpoint;
    private final String streetViewMetadataEndpoint;
    private final int minimumElevationMeters;
    private final int streetViewRadiusMeters;
    private final int maxAttemptsPerRegion;
    private final String streetViewSource;

    @Autowired
    public GooglePanoramaService(
            GoogleMapsHttpClient googleMapsHttpClient,
            GoogleMapsApiKeyProvider googleMapsApiKeyProvider,
            @Value("${google.maps.elevation-endpoint}") String elevationEndpoint,
            @Value("${google.maps.streetview-metadata-endpoint}") String streetViewMetadataEndpoint,
            @Value("${google.maps.minimum-elevation-meters}") int minimumElevationMeters,
            @Value("${google.maps.streetview-radius-meters}") int streetViewRadiusMeters,
            @Value("${google.maps.max-attempts-per-region}") int maxAttemptsPerRegion,
            @Value("${google.maps.streetview-source}") String streetViewSource) {
        this(
                googleMapsHttpClient,
                googleMapsApiKeyProvider,
                new SecureRandom(),
                elevationEndpoint,
                streetViewMetadataEndpoint,
                minimumElevationMeters,
                streetViewRadiusMeters,
                maxAttemptsPerRegion,
                streetViewSource);
    }

    GooglePanoramaService(
            GoogleMapsHttpClient googleMapsHttpClient,
            GoogleMapsApiKeyProvider googleMapsApiKeyProvider,
            Random random,
            String elevationEndpoint,
            String streetViewMetadataEndpoint,
            int minimumElevationMeters,
            int streetViewRadiusMeters,
            int maxAttemptsPerRegion,
            String streetViewSource) {
        this.googleMapsHttpClient = googleMapsHttpClient;
        this.googleMapsApiKeyProvider = googleMapsApiKeyProvider;
        this.objectMapper = new ObjectMapper();
        this.random = random;
        this.elevationEndpoint = elevationEndpoint;
        this.streetViewMetadataEndpoint = streetViewMetadataEndpoint;
        this.minimumElevationMeters = minimumElevationMeters;
        this.streetViewRadiusMeters = streetViewRadiusMeters;
        this.maxAttemptsPerRegion = maxAttemptsPerRegion;
        this.streetViewSource = streetViewSource;
    };

    public List<SearchRegion> getSearchRegionsFromString(String searchRegion) {
        if (searchRegion.isEmpty()) {
            List<SearchRegion> regions = GameRegions.getAllRegionsList();
            return regions;
        }
        List<SearchRegion> regions = GameRegions.getRegions(searchRegion);
        if (regions.isEmpty()) {
            String availableRegions = String.join(", ", GameRegions.getRegionMap().keySet());
            throw new ResponseStatusException(NOT_FOUND, "The provided region was not found! Available regions: " + availableRegions);
        }
        return regions;
    }

    public GooglePanoramaCandidate fetchPanoramaCandidate(List<SearchRegion> searchRegions) throws ResponseStatusException{
        if (searchRegions.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "No panorama search regions are configured.");
        }
        List<Integer> randomIndices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            randomIndices.add(random.nextInt(searchRegions.size()));
        }

        String apiKey = googleMapsApiKeyProvider.getApiKey();
        for (int index : randomIndices) {
            SearchRegion region = searchRegions.get(index);
            GooglePanoramaCandidate candidate = tryFindPanoramaInRegion(region, apiKey);
            if (candidate != null) {
                return candidate;
            }
        }

        throw new ResponseStatusException(NOT_FOUND,
                "No Street View panorama found in the configured mountain regions.");
    }

    private GooglePanoramaCandidate tryFindPanoramaInRegion(SearchRegion region, String apiKey) {
        for (int attempt = 0; attempt < maxAttemptsPerRegion; attempt++) {
            SamplePoint point = randomPoint(region);

            GooglePanoramaCandidate candidate = lookupPanorama(point, apiKey);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private SamplePoint randomPoint(SearchRegion region) {
        double latitude = region.minLatitude() + random.nextDouble() * (region.maxLatitude() - region.minLatitude());
        double longitude = region.minLongitude()
                + random.nextDouble() * (region.maxLongitude() - region.minLongitude());
        return new SamplePoint(latitude, longitude);
    }

    private boolean passesElevationThreshold(SamplePoint point, String apiKey) {
        URI requestUri = URI.create(elevationEndpoint
                + "?locations=" + encodeCoordinatePair(point.latitude(), point.longitude())
                + "&key=" + encodeValue(apiKey));

        JsonNode response = parseJson(googleMapsHttpClient.get(requestUri));
        String status = response.path("status").asString();
        if ("OK".equals(status)) {
            JsonNode firstResult = response.path("results").path(0);
            double elevation = firstResult.path("elevation").asDouble(Double.NaN);
            return Double.isFinite(elevation) && elevation >= minimumElevationMeters;
        }
        if ("DATA_NOT_AVAILABLE".equals(status)) {
            return false;
        }

        String message = response.path("error_message").asString();
        throw googleMapsError("Elevation API", status, message);
    }

    private GooglePanoramaCandidate lookupPanorama(SamplePoint point, String apiKey) {
        StringBuilder uriBuilder = new StringBuilder(streetViewMetadataEndpoint)
                .append("?location=")
                .append(encodeCoordinatePair(point.latitude(), point.longitude()))
                .append("&radius=")
                .append(streetViewRadiusMeters)
                .append("&key=")
                .append(encodeValue(apiKey));

        if (streetViewSource != null && !streetViewSource.isBlank()) {
            uriBuilder.append("&source=").append(encodeValue(streetViewSource));
        }

        JsonNode response = parseJson(googleMapsHttpClient.get(URI.create(uriBuilder.toString())));
        String status = response.path("status").asString();
        if ("OK".equals(status)) {
            String panoId = response.path("pano_id").asString();
            JsonNode location = response.path("location");
            double latitude = location.path("lat").asDouble(Double.NaN);
            double longitude = location.path("lng").asDouble(Double.NaN);

            if (panoId.isBlank() || !Double.isFinite(latitude) || !Double.isFinite(longitude)) {
                return null;
            }
            return new GooglePanoramaCandidate(panoId, latitude, longitude);
        }
        if ("ZERO_RESULTS".equals(status) || "NOT_FOUND".equals(status)) {
            return null;
        }

        String message = response.path("error_message").asString();
        throw googleMapsError("Street View metadata", status, message);
    }

    private JsonNode parseJson(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (JacksonException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Received invalid JSON from Google Maps.", exception);
        }
    }

    private ResponseStatusException googleMapsError(String apiName, String status, String message) {
        String detail = apiName + " request failed with status " + status;
        if (message != null && !message.isBlank()) {
            detail = detail + ": " + message;
        }
        return new ResponseStatusException(BAD_GATEWAY, detail);
    }

    private String encodeCoordinatePair(double latitude, double longitude) {
        return String.format(Locale.ROOT, "%.6f,%.6f", latitude, longitude);
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    record SamplePoint(double latitude, double longitude) {
    }
}
