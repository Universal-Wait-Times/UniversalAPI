package me.matthewe.universal.universalapi.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Log
@Service
public class AttractionWebhookClient {

    private final WebClient webClient;
    private final Queue<AttractionUpdate> queue = new ConcurrentLinkedQueue<>();
    private volatile boolean serviceHealthy = false;

    public AttractionWebhookClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                ))
                .baseUrl("http://localhost:9506")
                .build();
    }

    public void queueAttractionStatus(Attraction oldAttraction, Attraction attraction) {
        queue.add(new AttractionUpdate(oldAttraction, attraction));
    }

    @Scheduled(fixedDelay = 10000)
    public void processQueue() {
        if (!serviceHealthy) {
            log.warning("Webhook service is down. Skipping queue processing.");
            return;
        }
        AttractionUpdate update;
        while ((update = queue.poll()) != null) {
            sendAttractionStatus(update.oldAttraction, update.attraction).subscribe(
                    success -> log.info("Webhook sent successfully: " + success),
                    error -> log.severe("Webhook failed: " + error.getMessage())
            );
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void checkHealth() {
        webClient.get()
                .uri("/api/v1/discord/ride_alerts/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3))
                .doOnError(err -> {
                    log.warning("Health check failed: " + err.getMessage());
                    serviceHealthy = false;
                })
                .subscribe(body -> {
                    log.info("Health check OK");
                    serviceHealthy = true;
                });
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
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonBody))
                .retrieve()
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.severe("Error response: " + errorBody);
                            return Mono.error(new RuntimeException("HTTP " + response.statusCode()));
                        }))
                .bodyToMono(String.class)
                .doOnError(e -> log.severe("Webhook failed: " + e.getMessage()));
    }

    private static class AttractionUpdate {
        Attraction oldAttraction;
        Attraction attraction;

        public AttractionUpdate(Attraction oldAttraction, Attraction attraction) {
            this.oldAttraction = oldAttraction;
            this.attraction = attraction;
        }
    }
}
