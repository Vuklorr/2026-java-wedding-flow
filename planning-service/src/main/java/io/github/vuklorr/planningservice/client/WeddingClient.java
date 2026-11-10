package io.github.vuklorr.planningservice.client;

import io.github.vuklorr.planningservice.exception.WeddingNotFoundException;
import io.github.vuklorr.planningservice.exception.WeddingServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Component
public class WeddingClient {

    private final RestClient restClient;

    public WeddingClient(
            RestClient.Builder builder,
            @Value("${clients.wedding.base-url}") String baseUrl
    ) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }


    public void ensureWeddingExists(int weddingId) {
        try {
            restClient.get()
                    .uri("/weddings/{id}", weddingId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound e) {
            throw new WeddingNotFoundException(weddingId);

        } catch (RestClientException e) {
            throw new WeddingServiceUnavailableException();
        }
    }
}
