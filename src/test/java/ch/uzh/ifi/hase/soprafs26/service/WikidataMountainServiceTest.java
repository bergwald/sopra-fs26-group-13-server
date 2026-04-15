package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

public class WikidataMountainServiceTest {

    private WikidataClient wikidataClient;

    @BeforeEach
    void setUp() {
        wikidataClient = mock(WikidataClient.class);
    }

    @Test
    void fetchMountainCandidate_returnsRandomMappedMountainFromSelectedRegion() {
        WikidataMountainService wikidataMountainService = new WikidataMountainService(
                wikidataClient,
                new FixedRandom(0, 1),
                List.of(new WikidataMountainService.SearchRegion("TestRange", 1.0, 2.0, 3.0, 4.0)));

        when(wikidataClient.executeSelectQuery(anyString()))
                .thenReturn("""
                        {
                          "results": {
                            "bindings": [
                              {
                                "mountain": { "value": "http://www.wikidata.org/entity/Q1" },
                                "mountainLabel": { "value": "Mountain One" },
                                "image": { "value": "https://commons.wikimedia.org/wiki/Special:FilePath/MountainOne.jpg" },
                                "location": { "value": "Point(7.658611111 45.976388888)" }
                              },
                              {
                                "mountain": { "value": "http://www.wikidata.org/entity/Q2" },
                                "mountainLabel": { "value": "Mountain Two" },
                                "image": { "value": "https://commons.wikimedia.org/wiki/Special:FilePath/MountainTwo.jpg" },
                                "location": { "value": "Point(8.005277778 46.5775)" }
                              }
                            ]
                          }
                        }
                        """);

        WikidataMountainCandidate candidate = wikidataMountainService.fetchMountainCandidate();
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        verify(wikidataClient).executeSelectQuery(queryCaptor.capture());
        assertEquals("Q2", candidate.wikidataEntityId());
        assertEquals("Mountain Two", candidate.mountainName());
        assertEquals("https://commons.wikimedia.org/wiki/Special:FilePath/MountainTwo.jpg", candidate.imageUrl());
        assertEquals(46.5775, candidate.latitude());
        assertEquals(8.005277778, candidate.longitude());
        assertEquals(true, queryCaptor.getValue().contains("\"Point(1.000000 2.000000)\"^^geo:wktLiteral"));
        assertEquals(true, queryCaptor.getValue().contains("\"Point(3.000000 4.000000)\"^^geo:wktLiteral"));
    }

    @Test
    void fetchMountainCandidate_fallsBackToNextRegionWhenSelectedRegionHasNoUsableCandidate() {
        WikidataMountainService wikidataMountainService = new WikidataMountainService(
                wikidataClient,
                new FixedRandom(0, 0),
                List.of(
                        new WikidataMountainService.SearchRegion("EmptyRegion", 1.0, 2.0, 3.0, 4.0),
                        new WikidataMountainService.SearchRegion("WorkingRegion", 10.0, 20.0, 30.0, 40.0)));

        when(wikidataClient.executeSelectQuery(anyString()))
                .thenReturn("""
                        {
                          "results": {
                            "bindings": []
                          }
                        }
                        """, """
                        {
                          "results": {
                            "bindings": [
                              {
                                "mountain": { "value": "http://www.wikidata.org/entity/Q2" },
                                "mountainLabel": { "value": "Eiger" },
                                "image": { "value": "https://commons.wikimedia.org/wiki/Special:FilePath/Eiger.jpg" },
                                "location": { "value": "Point(8.005277778 46.5775)" }
                              }
                            ]
                          }
                        }
                        """);

        WikidataMountainCandidate candidate = wikidataMountainService.fetchMountainCandidate();
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        verify(wikidataClient, times(2)).executeSelectQuery(queryCaptor.capture());
        assertEquals("Q2", candidate.wikidataEntityId());
        assertEquals("Eiger", candidate.mountainName());
        assertEquals(true, queryCaptor.getAllValues().get(0).contains("\"Point(1.000000 2.000000)\"^^geo:wktLiteral"));
        assertEquals(true,
                queryCaptor.getAllValues().get(1).contains("\"Point(10.000000 20.000000)\"^^geo:wktLiteral"));
    }

    @Test
    void fetchMountainCandidate_whenNoUsableResultAcrossRegions_thenThrowNotFound() {
        WikidataMountainService wikidataMountainService = new WikidataMountainService(
                wikidataClient,
                new FixedRandom(0),
                List.of(
                        new WikidataMountainService.SearchRegion("RegionA", 1.0, 2.0, 3.0, 4.0),
                        new WikidataMountainService.SearchRegion("RegionB", 5.0, 6.0, 7.0, 8.0)));

        when(wikidataClient.executeSelectQuery(anyString()))
                .thenReturn("""
                        {
                          "results": {
                            "bindings": []
                          }
                        }
                        """);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> wikidataMountainService.fetchMountainCandidate());

        assertEquals(404, exception.getStatusCode().value());
    }

    private static final class FixedRandom extends Random {
        private final int[] values;
        private int index;

        private FixedRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            int value = values[index % values.length];
            index++;
            return value;
        }
    }
}
