package me.matthewe.universal.universalapi.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        Map<String, Object> body = new HashMap<>();
        if (oldAttraction != null) {
            body.put("oldAttraction", oldAttraction);
        }
        body.put("attraction", attraction);
        body.put("key", System.getenv("API_KEY"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(body);
            log.info("Final JSON body: " + jsonBody);
        } catch (JsonProcessingException e) {
            log.severe("JSON serialization failed: " + e.getMessage());
            return Mono.error(e);
        }

        return webClient.post()
                .uri("/api/v1/discord/ride_alerts")
                .header("Content-Type", "application/json")
                .bodyValue(jsonBody) // <-- raw JSON string
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.severe("Error response: " + errorBody);
                            return Mono.error(new RuntimeException("HTTP " + response.statusCode()));
                        }))
                .bodyToMono(String.class)
                .doOnError(e -> log.severe("Webhook failed: " + e.getMessage()));
    }
}