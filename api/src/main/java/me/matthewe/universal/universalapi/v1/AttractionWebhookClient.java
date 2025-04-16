package me.matthewe.universal.universalapi.v1;

import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log
@Service
public class AttractionWebhookClient {

    private final WebClient webClient;


    public AttractionWebhookClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:9506") // Change to your actual host if needed
                .build();
    }

    public Mono<String> sendAttractionStatus(Attraction oldAttraction, Attraction attraction) {
        log.info("Update status of attraction " + attraction.getWaitTimeAttractionId());
        Map<String, Object> body =new HashMap<>();
        if (oldAttraction != null) {
            body.put("oldAttraction", oldAttraction);
        }
        if (attraction!=null){
            body.put("attraction", attraction);
        }
        body.put("key", System.getenv("API_KEY"));


        log.info("Sending to Discord: " + attraction.getWaitTimeAttractionId());
        log.info("Key: " + System.getenv("API_KEY"));
        log.info("Old Attraction: " + oldAttraction);
        log.info("New Attraction: " + attraction);


        Mono<String> stringMono = webClient.post()
                .uri("/api/v1/discord/ride_alerts")
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is5xxServerError() || status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.severe("Failed to publish JSON payload. Response code: " + response.statusCode());
                                    log.severe("Error body: " + errorBody);
                                    return Mono.error(new RuntimeException("Failed with status: " + response.statusCode()));
                                }))
                .bodyToMono(String.class);

        return stringMono;
    }
}