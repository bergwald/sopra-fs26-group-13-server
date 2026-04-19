package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URI;

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
public class GoogleMapsWebServiceClient implements GoogleMapsHttpClient {

    private final RestClient restClient;

    public GoogleMapsWebServiceClient(@Value("${google.maps.timeout-ms}") int timeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String get(URI uri) {
        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException exception) {
            String message = "Failed to fetch data from Google Maps. Upstream status: "
                    + exception.getStatusCode().value();
            throw new ResponseStatusException(BAD_GATEWAY, message, exception);
        } catch (RestClientException exception) {
            String message = "Failed to fetch data from Google Maps. " + exception.getClass().getSimpleName()
                    + ": " + exception.getMessage();
            throw new ResponseStatusException(BAD_GATEWAY, message, exception);
        }
    }
}
