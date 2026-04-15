package ch.uzh.ifi.hase.soprafs26.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.service.WikidataMountainCandidate;
import ch.uzh.ifi.hase.soprafs26.service.WikidataMountainService;

@WebMvcTest(WikidataController.class)
public class WikidataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WikidataMountainService wikidataMountainService;

    @Test
    void givenMountainCandidate_whenGetWikidataMountain_thenReturnJson() throws Exception {
        given(wikidataMountainService.fetchMountainCandidate()).willReturn(
                new WikidataMountainCandidate(
                        "Q513",
                        "Matterhorn",
                        "https://commons.wikimedia.org/wiki/Special:FilePath/Matterhorn.jpg",
                        45.976388888,
                        7.658611111));

        MockHttpServletRequestBuilder getRequest = get("/wikidata/mountain").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wikidataEntityId", is("Q513")))
                .andExpect(jsonPath("$.mountainName", is("Matterhorn")))
                .andExpect(jsonPath("$.imageUrl",
                        is("https://commons.wikimedia.org/wiki/Special:FilePath/Matterhorn.jpg")))
                .andExpect(jsonPath("$.latitude", is(45.976388888)))
                .andExpect(jsonPath("$.longitude", is(7.658611111)));
    }

    @Test
    void givenUpstreamFailure_whenGetWikidataMountain_thenReturnBadGateway() throws Exception {
        given(wikidataMountainService.fetchMountainCandidate())
                .willThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch data from Wikidata."));

        MockHttpServletRequestBuilder getRequest = get("/wikidata/mountain").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest).andExpect(status().isBadGateway());
    }
}
