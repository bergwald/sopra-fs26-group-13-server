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

import ch.uzh.ifi.hase.soprafs26.service.GooglePanoramaCandidate;
import ch.uzh.ifi.hase.soprafs26.service.GooglePanoramaService;

@WebMvcTest(GooglePanoramaController.class)
public class GooglePanoramaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GooglePanoramaService googlePanoramaService;

    @Test
    void givenPanoramaCandidate_whenGetGooglePanorama_thenReturnJson() throws Exception {
        given(googlePanoramaService.fetchPanoramaCandidate()).willReturn(
                new GooglePanoramaCandidate("pano-123", 46.5775, 8.005277778));

        MockHttpServletRequestBuilder getRequest = get("/google/panorama").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider", is("google-street-view")))
                .andExpect(jsonPath("$.panoId", is("pano-123")))
                .andExpect(jsonPath("$.latitude", is(46.5775)))
                .andExpect(jsonPath("$.longitude", is(8.005277778)));
    }

    @Test
    void givenUpstreamFailure_whenGetGooglePanorama_thenReturnBadGateway() throws Exception {
        given(googlePanoramaService.fetchPanoramaCandidate())
                .willThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch data from Google Maps."));

        MockHttpServletRequestBuilder getRequest = get("/google/panorama").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest).andExpect(status().isBadGateway());
    }
}
