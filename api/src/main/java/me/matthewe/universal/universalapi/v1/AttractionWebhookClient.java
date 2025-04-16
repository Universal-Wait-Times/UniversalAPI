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

            URL url = new URL("http://localhost:9506/api/v1/discord/ride_alerts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int status = conn.getResponseCode();
            log.info("HTTP response code: " + status);

            if (status >= 200 && status < 300) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                    log.severe("Error response: " + errorResponse);
                    throw new IOException("HTTP error: " + status);
                }
            }
        } catch (Exception e) {
            log.severe("Failed to send webhook: " + e.getMessage());
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
