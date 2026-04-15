package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.WikidataMountainGetDTO;
import ch.uzh.ifi.hase.soprafs26.service.WikidataMountainCandidate;
import ch.uzh.ifi.hase.soprafs26.service.WikidataMountainService;

@RestController
public class WikidataController {

    private final WikidataMountainService wikidataMountainService;

    public WikidataController(WikidataMountainService wikidataMountainService) {
        this.wikidataMountainService = wikidataMountainService;
    }

    @GetMapping("/wikidata/mountain")
    @ResponseStatus(HttpStatus.OK)
    public WikidataMountainGetDTO getMountainCandidate() {
        WikidataMountainCandidate candidate = wikidataMountainService.fetchMountainCandidate();
        WikidataMountainGetDTO dto = new WikidataMountainGetDTO();
        dto.setWikidataEntityId(candidate.wikidataEntityId());
        dto.setMountainName(candidate.mountainName());
        dto.setImageUrl(candidate.imageUrl());
        dto.setLatitude(candidate.latitude());
        dto.setLongitude(candidate.longitude());
        return dto;
    }
}
