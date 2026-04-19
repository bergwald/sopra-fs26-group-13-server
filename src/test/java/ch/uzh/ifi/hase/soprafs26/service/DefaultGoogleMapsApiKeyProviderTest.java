package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

public class DefaultGoogleMapsApiKeyProviderTest {

    @Test
    void getApiKey_prefersConfiguredLocalApiKey() {
        SecretVersionAccessor secretVersionAccessor = mock(SecretVersionAccessor.class);
        DefaultGoogleMapsApiKeyProvider provider = new DefaultGoogleMapsApiKeyProvider(
                "local-api-key",
                "projects/demo/secrets/google-maps-server-api-key/versions/latest",
                secretVersionAccessor);

        String apiKey = provider.getApiKey();

        assertEquals("local-api-key", apiKey);
    }

    @Test
    void getApiKey_fetchesAndCachesSecretManagerValue() {
        SecretVersionAccessor secretVersionAccessor = mock(SecretVersionAccessor.class);
        when(secretVersionAccessor.accessSecretVersion(
                "projects/demo/secrets/google-maps-server-api-key/versions/latest"))
                .thenReturn("secret-manager-api-key");

        DefaultGoogleMapsApiKeyProvider provider = new DefaultGoogleMapsApiKeyProvider(
                "",
                "projects/demo/secrets/google-maps-server-api-key/versions/latest",
                secretVersionAccessor);

        String first = provider.getApiKey();
        String second = provider.getApiKey();

        assertEquals("secret-manager-api-key", first);
        assertEquals("secret-manager-api-key", second);
        verify(secretVersionAccessor, times(1))
                .accessSecretVersion("projects/demo/secrets/google-maps-server-api-key/versions/latest");
    }

    @Test
    void getApiKey_whenNothingConfigured_thenThrowServiceUnavailable() {
        SecretVersionAccessor secretVersionAccessor = mock(SecretVersionAccessor.class);
        DefaultGoogleMapsApiKeyProvider provider = new DefaultGoogleMapsApiKeyProvider(
                "",
                "",
                secretVersionAccessor);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, provider::getApiKey);

        assertEquals(503, exception.getStatusCode().value());
    }
}
