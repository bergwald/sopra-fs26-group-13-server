package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
public class WikidataHttpClient implements WikidataClient {

    private final RestClient restClient;
    private final String queryEndpoint;

    public WikidataHttpClient(
            @Value("${wikidata.query-endpoint}") String queryEndpoint,
            @Value("${wikidata.timeout-ms}") int timeoutMs,
            @Value("${wikidata.user-agent}") String userAgent) {
        this.queryEndpoint = queryEndpoint;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .defaultHeader(HttpHeaders.ACCEPT, "application/sparql-results+json, application/json")
                .build();
    }

    @Override
    public String executeSelectQuery(String sparqlQuery) {
        try {
            String encodedQuery = URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8);
            URI requestUri = URI.create(queryEndpoint + "?query=" + encodedQuery + "&format=json");
            return restClient.get()
                    .uri(requestUri)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            String message = "Failed to fetch data from Wikidata. Upstream status: "
                    + exception.getStatusCode().value();
            throw new ResponseStatusException(BAD_GATEWAY, message, exception);
        } catch (RestClientException exception) {
            String message = "Failed to fetch data from Wikidata. " + exception.getClass().getSimpleName()
                    + ": " + exception.getMessage();
            throw new ResponseStatusException(BAD_GATEWAY, message, exception);
        }
    }
}
