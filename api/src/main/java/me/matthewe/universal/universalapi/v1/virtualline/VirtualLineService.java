package me.matthewe.universal.universalapi.v1.virtualline;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Log
@Service
public class VirtualLineService {
    private final WebClient webClient = WebClient.builder()
            .baseUrl(System.getenv("UNIVERSAL_ENDPOINT_VIRTUAL_QUEUE"))

            .build();

    private AttractionWebhookClient attractionWebhookClient;
    private final Map<String, List<VirtualLine>> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdatedCache = new ConcurrentHashMap<>();
    private final Random random = new Random();


    public VirtualLineService(AttractionWebhookClient attractionWebhookClient) {
        this.attractionWebhookClient = attractionWebhookClient;
    }

    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(() -> loopUpdate("Orlando"));
//        Schedulers.parallel().schedule(() -> loopUpdate("Hollywood"));
    }

    private void loopUpdate(String city) {
        updateCache(city);
        long delay = 4000 + random.nextInt(2602);
        Mono.delay(Duration.ofMillis(delay))
                .doOnNext(i -> loopUpdate(city))
                .subscribe();
    }

    private void updateCache(String city) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/Queues")
                        .queryParam("city", city)
                        .queryParam("pageSize", "All")
                        .build())
                .retrieve()
                .onStatus(status -> status.value() == 401, clientResponse -> {
                    log.warning("❌ [" + city + "] Unauthorized (401) when fetching virtual line data.");
                    return Mono.empty(); // Swallow error, proceed without crashing
                })
                .bodyToMono(VirtualLineResponse.class)
                .doOnError(e -> log.warning("❌ Failed to update cache for " + city + ": " + e.getMessage()))
                .map(VirtualLineResponse::getResults)
                .subscribe(newData -> {
                    List<VirtualLine> oldData = null;
                    if (dataCache.containsKey(city)) {

                        oldData = dataCache.get(city);

                    }
                    dataCache.put(city, newData);
                    lastUpdatedCache.put(city, System.currentTimeMillis());
                    onVirtualLineUpdate(city, oldData, newData);
                });
    }

    private void onVirtualLineUpdate(String city, List<VirtualLine> oldData, List<VirtualLine> newData) {
        if (oldData != null) {
            for (VirtualLine oldDatum : oldData) {
                for (VirtualLine newDatum : newData) {
                    if (oldDatum.getName().equals(newDatum.getName())) {
                        onUpdate(city, oldDatum, newDatum);
                    }
                }
            }
        } else {
            for (VirtualLine newDatum : newData) {
                onUpdate(city, null, newDatum);
            }
        }
    }

    private void onUpdate(String city, VirtualLine oldDatum, VirtualLine newDatum) {

        if (!newDatum.isEnabled() && !newDatum.isUnavailable() && !newDatum.isProfileAware()) {
            return;
        }
        if (newDatum.getId().equals("10102")) return;
        if (newDatum.getId().equals("10101")) return;

        attractionWebhookClient.sendVirtualQueueStatus(oldDatum, newDatum);

    }

    public List<VirtualLine> getCachedData(String city) {
        return dataCache.getOrDefault(city, List.of());
    }

    public long getLastUpdated(String city) {
        return lastUpdatedCache.getOrDefault(city, 0L);
    }
}