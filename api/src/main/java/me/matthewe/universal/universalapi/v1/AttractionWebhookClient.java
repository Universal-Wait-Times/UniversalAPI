package me.matthewe.universal.universalapi.v1;

import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

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
        return webClient.post()
                .uri("/api/v1/discord/ride_alerts")
                .bodyValue(Map.of(
                        "oldAttraction", oldAttraction,
                        "attraction", attraction,
                        "key",  System.getenv("API_KEY")
                ))
                .retrieve()
                .bodyToMono(String.class);
    }
}