package me.matthewe.universal.universalapi.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
    private final Queue<VirtualLineUpdate> lineUpdateQueue = new ConcurrentLinkedQueue<>();
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

    public void queueVirtualLineStatus(VirtualLine oldAttraction, VirtualLine attraction) {
        lineUpdateQueue.add(new VirtualLineUpdate(oldAttraction, attraction));
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

        VirtualLineUpdate update1;
        while ((update1 = lineUpdateQueue.poll()) != null) {
            sendVirtualQueueStatus(update1.oldAttraction, update1.attraction);
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void checkHealth() {
        webClient.get()
                .uri("/api/v1/discord/ride_alerts/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(e -> Mono.empty())
                .onErrorComplete(throwable -> {
                    serviceHealthy = false;
                    return true;
                })
                .doOnError(err -> {
                    log.warning("Health check failed:");
                    serviceHealthy = false;
                })
                .subscribe(body -> {
//                    log.info("Health check OK");
                    serviceHealthy = true;
                });
    }

    public void sendVirtualQueueStatus(VirtualLine oldLine, VirtualLine newLine) {

        if (!serviceHealthy) {
            queueVirtualLineStatus(oldLine, newLine);
            return;
        }

        Map<String, Object> body = new HashMap<>();
        if (oldLine != null) {
            body.put("oldLine", oldLine);
        }
        body.put("newLine", newLine);
        body.put("key", System.getenv("API_KEY"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            String jsonBody = mapper.writeValueAsString(body);
//            log.info("Final JSON body: " + jsonBody);

             webClient.post()
                    .uri("/api/v1/discord/line_alerts")
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
                    .doOnNext(response -> {})
                     .doOnError(error -> log.warning("❌ Webhook call failed: " + error.getMessage()))
                     .subscribe();
        } catch (JsonProcessingException e) {
            log.severe("Failed to serialize body: " + e.getMessage());
            return ;
        } catch (Exception e) {
            log.severe("Error during webhook call: " + e.getMessage());
            return ;
        }
    }
    public String sendAttractionStatus(Attraction oldAttraction, Attraction attraction) {
        log.info("Update status of attraction " + attraction.getWaitTimeAttractionId());

        if (!serviceHealthy) {
            log.warning("Webhook unhealthy — requeuing update for " + attraction.getDisplayName());
            queueAttractionStatus(oldAttraction, attraction);
            return "NOT READY";
        }

        Map<String, Object> body = new HashMap<>();
        if (oldAttraction != null) {
            body.put("oldAttraction", oldAttraction);
        }
        body.put("attraction", attraction);
        body.put("key", System.getenv("API_KEY"));

        return webClient.post()
                .uri("/api/v1/discord/ride_alerts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body) // ✅ Corrected here
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
                .doOnNext(response -> log.info("✅ Webhook response: " + response))
                .doOnError(error -> log.warning("❌ Webhook failed: " + error.getMessage()))
                .block(Duration.ofSeconds(3));
    }



    @AllArgsConstructor
    private static class AttractionUpdate {
        Attraction oldAttraction;
        Attraction attraction;
    }

    @AllArgsConstructor
    private static class VirtualLineUpdate {
        VirtualLine oldAttraction;
        VirtualLine attraction;


    }
}
