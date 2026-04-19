package ch.uzh.ifi.hase.soprafs26.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class SecretManagerSecretVersionAccessor implements SecretVersionAccessor {

    @Override
    public String accessSecretVersion(String secretVersionName) {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            AccessSecretVersionResponse response = client.accessSecretVersion(
                    AccessSecretVersionRequest.newBuilder()
                            .setName(secretVersionName)
                            .build());

            return response.getPayload().getData().toStringUtf8();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Failed to initialize Secret Manager client.",
                    exception);
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Failed to access Google Maps API key from Secret Manager.",
                    exception);
        }
    }
}
