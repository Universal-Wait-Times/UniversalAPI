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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
            sendAttractionStatus(update.oldAttraction, update.attraction);
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

    public String sendAttractionStatus(Attraction oldAttraction, Attraction attraction) {
        log.info("Update status of attraction " + attraction.getWaitTimeAttractionId());

        if (!serviceHealthy) {
            queueAttractionStatus(oldAttraction, attraction);
            return "NOT READY";
        }

        Map<String, Object> body = new HashMap<>();
        if (oldAttraction != null) {
            body.put("oldAttraction", oldAttraction);
        }
        body.put("attraction", attraction);
        body.put("key", System.getenv("API_KEY"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            String jsonBody = mapper.writeValueAsString(body);
            log.info("Final JSON body: " + jsonBody);

            return webClient.post()
                    .uri("/api/v1/discord/ride_alerts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(jsonBody)
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            response -> {
                                log.severe("Failed with status code: " + response.statusCode());
                                return response.bodyToMono(String.class)
                                        .doOnNext(errorBody -> log.severe("Error response body: " + errorBody))
                                        .then(Mono.error(new RuntimeException("Non-successful response")));
                            }
                    )
                    .bodyToMono(String.class)
                    .doOnNext(response -> log.info("Webhook response: " + response))
                    .block(Duration.ofSeconds(3));
        } catch (JsonProcessingException e) {
            log.severe("Failed to serialize body: " + e.getMessage());
            return null;
        } catch (Exception e) {
            log.severe("Error during webhook call: " + e.getMessage());
            return null;
        }
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
