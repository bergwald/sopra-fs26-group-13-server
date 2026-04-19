package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.GooglePanoramaGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.GooglePanoramaCandidate;
import ch.uzh.ifi.hase.soprafs26.service.GooglePanoramaService;

@RestController
public class GooglePanoramaController {

    private final GooglePanoramaService googlePanoramaService;

    public GooglePanoramaController(GooglePanoramaService googlePanoramaService) {
        this.googlePanoramaService = googlePanoramaService;
    }

    // Temporary integration endpoint for the frontend Street View demo.
    // This should be removed once panorama selection is wired into the real
    // session/round game flow.
    @GetMapping("/google/panorama")
    @ResponseStatus(HttpStatus.OK)
    public GooglePanoramaGetDTO getPanoramaCandidate() {
        GooglePanoramaCandidate candidate = googlePanoramaService.fetchPanoramaCandidate();
        GooglePanoramaGetDTO dto = new GooglePanoramaGetDTO();
        dto.setProvider("google-street-view");
        dto.setPanoId(candidate.panoId());
        dto.setLatitude(candidate.latitude());
        dto.setLongitude(candidate.longitude());
        return dto;
    }
}
