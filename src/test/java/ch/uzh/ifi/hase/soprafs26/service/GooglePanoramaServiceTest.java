package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.SearchRegion;

public class GooglePanoramaServiceTest {

  private GoogleMapsHttpClient googleMapsHttpClient;
  private GoogleMapsApiKeyProvider googleMapsApiKeyProvider;
  private GooglePanoramaService mockGooglePanoramaService;

  private List<SearchRegion> mockSearchRegion;
  private List<SearchRegion> mockSearchRegions;

  @BeforeEach
  void setUp() {
    googleMapsHttpClient = mock(GoogleMapsHttpClient.class);
    googleMapsApiKeyProvider = mock(GoogleMapsApiKeyProvider.class);
    when(googleMapsApiKeyProvider.getApiKey()).thenReturn("server-key");
    mockSearchRegion = List.of(new SearchRegion("Alps", 6.0, 45.0, 7.0, 46.0));
    mockSearchRegions = List.of(
        new SearchRegion("LowRegion", 6.0, 45.0, 7.0, 46.0),
        new SearchRegion("HighRegion", 10.0, 47.0, 11.0, 48.0));
    mockGooglePanoramaService = new GooglePanoramaService(
        googleMapsHttpClient,
        googleMapsApiKeyProvider,
        new FixedRandom(0, 0, 0),
        "https://maps.googleapis.com/maps/api/elevation/json",
        "https://maps.googleapis.com/maps/api/streetview/metadata",
        1000,
        3000,
        1,
        "default");

  }

  @Test
  void getSearchRegionsFromString_validRegion() {
    List<SearchRegion> result = mockGooglePanoramaService.getSearchRegionsFromString("Alps");
    assertEquals(10, result.size());
  }

  @Test
  void getSearchRegionsFromString_validEmpty() {
    List<SearchRegion> result = mockGooglePanoramaService.getSearchRegionsFromString("");
    assertEquals(21, result.size());
  }

  @Test
  void getSearchRegionsFromString_invalidRegion() {

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> mockGooglePanoramaService.getSearchRegionsFromString("SomeRandomRegion"));
    assertEquals(404, exception.getStatusCode().value());
  }
/*
  @Test
  void fetchPanoramaCandidate_returnsPanoramaFromSelectedRegion() {

    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [
                {
                  "elevation": 1920.4
                }
              ],
              "status": "OK"
            }
            """, """
            {
              "copyright": "Google",
              "date": "2024-06",
              "location": {
                "lat": 45.321,
                "lng": 6.654
              },
              "pano_id": "test-pano-id",
              "status": "OK"
            }
            """);
    GooglePanoramaCandidate candidate = mockGooglePanoramaService.fetchPanoramaCandidate(mockSearchRegion);

    assertEquals("test-pano-id", candidate.panoId());
    assertEquals(45.321, candidate.latitude());
    assertEquals(6.654, candidate.longitude());
  }

  @Test
  void fetchPanoramaCandidate_skipsLowAltitudePointsAndFallsBackToNextRegion() {

    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [
                {
                  "elevation": 500.0
                }
              ],
              "status": "OK"
            }
            """, """
            {
              "results": [
                {
                  "elevation": 2100.0
                }
              ],
              "status": "OK"
            }
            """, """
            {
              "location": {
                "lat": 47.123,
                "lng": 10.987
              },
              "pano_id": "fallback-pano",
              "status": "OK"
            }
            """);

    GooglePanoramaCandidate candidate = mockGooglePanoramaService.fetchPanoramaCandidate(mockSearchRegions);

    assertEquals("fallback-pano", candidate.panoId());
    assertEquals(47.123, candidate.latitude());
    assertEquals(10.987, candidate.longitude());
  }

  @Test
  void fetchPanoramaCandidate_buildsMetadataRequestWithRadiusAndSource() {

    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [
                {
                  "elevation": 2000.0
                }
              ],
              "status": "OK"
            }
            """, """
            {
              "location": {
                "lat": 45.123,
                "lng": 6.456
              },
              "pano_id": "pano-123",
              "status": "OK"
            }
            """);

    mockGooglePanoramaService.fetchPanoramaCandidate(mockSearchRegion);

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    org.mockito.Mockito.verify(googleMapsHttpClient, org.mockito.Mockito.times(2)).get(uriCaptor.capture());
    String metadataUri = uriCaptor.getAllValues().get(1).toString();

    assertEquals(true, metadataUri.contains("radius=3000"));
    assertEquals(true, metadataUri.contains("source=default"));
    assertEquals(true, metadataUri.contains("key=server-key"));
  }
*/
  @Test
  void fetchPanoramaCandidate_whenApiKeyProviderFails_thenThrowServiceUnavailable() {
    when(googleMapsApiKeyProvider.getApiKey())
        .thenThrow(new ResponseStatusException(
            org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
            "Google Maps API key is not configured locally or through Secret Manager."));

    GooglePanoramaService googlePanoramaService = new GooglePanoramaService(
        googleMapsHttpClient,
        googleMapsApiKeyProvider,
        new FixedRandom(0),
        "https://maps.googleapis.com/maps/api/elevation/json",
        "https://maps.googleapis.com/maps/api/streetview/metadata",
        1000,
        2500,
        1,
        "default");

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> googlePanoramaService.fetchPanoramaCandidate(mockSearchRegion));

    assertEquals(503, exception.getStatusCode().value());
  }

  @Test
  void fetchPanoramaCandidate_whenNoPanoramaFound_thenThrowNotFound() {
    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [
                {
                  "elevation": 2100.0
                }
              ],
              "status": "OK"
            }
            """, """
            {
              "status": "ZERO_RESULTS"
            }
            """);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> mockGooglePanoramaService.fetchPanoramaCandidate(List.of()));

    assertEquals(404, exception.getStatusCode().value());
  }

  @Test
  void testFetchPanoramaCandidate_invalidJsonError() {
    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [
                {
                  "Invalid json!"
                }
              ],
              "status": "SomeRandomError"

            """);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> mockGooglePanoramaService.fetchPanoramaCandidate(mockSearchRegion));
    assertEquals(502, exception.getStatusCode().value());
  }

  @Test
  void testFetchPanoramaCandidate_unexpectedError() {
    when(googleMapsHttpClient.get(org.mockito.ArgumentMatchers.any(URI.class)))
        .thenReturn("""
            {
              "results": [],
              "status": "SOME_ERROR",
              "error_message": "There went something wrong"
            }
            """);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> mockGooglePanoramaService.fetchPanoramaCandidate(mockSearchRegion));
    assertEquals(502, exception.getStatusCode().value());
  }

  private static final class FixedRandom extends Random {
    private final double[] values;
    private int index;

    private FixedRandom(double... values) {
      this.values = values;
    }

    @Override
    public int nextInt(int bound) {
      int value = (int) values[index % values.length];
      index++;
      return value;
    }

    @Override
    public double nextDouble() {
      double value = values[index % values.length];
      index++;
      return value;
    }
  }
}
