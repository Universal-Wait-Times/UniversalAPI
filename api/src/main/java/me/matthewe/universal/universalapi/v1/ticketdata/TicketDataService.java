
package me.matthewe.universal.universalapi.v1.ticketdata;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.ticketdata.TicketData;
import me.matthewe.universal.commons.ticketdata.TicketDataAPI;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import me.matthewe.universal.universalapi.v1.virtualline.VirtualLineResponse;
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
public class TicketDataService {

    private AttractionWebhookClient attractionWebhookClient;
    @Getter private final Map<String, TicketData> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdatedCache = new ConcurrentHashMap<>();
    private final Random random = new Random();


    public TicketDataService(AttractionWebhookClient attractionWebhookClient) {
        this.attractionWebhookClient = attractionWebhookClient;
    }

    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(this::loopUpdate);
    }

    private void loopUpdate() {
        updateCache();
        long delay = 4000 + random.nextInt(2602);
        Mono.delay(Duration.ofMillis(delay))
                .doOnNext(i -> loopUpdate())
                .subscribe();
    }

    private TicketDataAPI api = new TicketDataAPI();


    private void updateCache() {
        for (Map.Entry<String, TicketData> entry : api.pullTicketTable().entrySet()) {
            if (dataCache.containsKey(entry.getKey())) {
                onUpdate(dataCache.get(entry.getKey()), entry.getValue());
            } else {
                onUpdate(null, entry.getValue());
            }
            dataCache.put(entry.getKey(), entry.getValue());
            lastUpdatedCache.put(entry.getKey(), System.currentTimeMillis());
        }
    }

    private void onUpdate(TicketData oldData, TicketData newData) {
        //TODO LATER IMPLEMENTATION
    }


    public long getLastUpdated(String date) {
        return lastUpdatedCache.getOrDefault(date, 0L);
    }
}
