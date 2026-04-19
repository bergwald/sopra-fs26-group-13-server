package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class DefaultGoogleMapsApiKeyProvider implements GoogleMapsApiKeyProvider {

    private final String configuredApiKey;
    private final String secretVersionName;
    private final SecretVersionAccessor secretVersionAccessor;

    private volatile String cachedSecretApiKey;

    public DefaultGoogleMapsApiKeyProvider(
            @Value("${google.maps.api-key:}") String configuredApiKey,
            @Value("${google.maps.api-key-secret-version:}") String secretVersionName,
            SecretVersionAccessor secretVersionAccessor) {
        this.configuredApiKey = configuredApiKey;
        this.secretVersionName = secretVersionName;
        this.secretVersionAccessor = secretVersionAccessor;
    }

    @Override
    public String getApiKey() {
        if (configuredApiKey != null && !configuredApiKey.isBlank()) {
            return configuredApiKey;
        }
        if (cachedSecretApiKey != null && !cachedSecretApiKey.isBlank()) {
            return cachedSecretApiKey;
        }
        if (secretVersionName == null || secretVersionName.isBlank()) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Google Maps API key is not configured locally or through Secret Manager.");
        }

        synchronized (this) {
            if (cachedSecretApiKey == null || cachedSecretApiKey.isBlank()) {
                String resolvedApiKey = secretVersionAccessor.accessSecretVersion(secretVersionName);
                if (resolvedApiKey == null || resolvedApiKey.isBlank()) {
                    throw new ResponseStatusException(
                            SERVICE_UNAVAILABLE,
                            "Google Maps API key secret is empty.");
                }
                cachedSecretApiKey = resolvedApiKey;
            }
        }

        return cachedSecretApiKey;
    }
}
