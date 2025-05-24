package me.matthewe.universal.universalapi.v1.ticketdata;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.ticketdata.TicketData;
import me.matthewe.universal.commons.ticketdata.TicketDataAPI;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Log
@Service
public class TicketDataService {

    private final AttractionWebhookClient attractionWebhookClient;
    private final CacheManager cacheManager;

    @Getter
    private final Map<String, TicketData> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdatedCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private final TicketDataAPI api = new TicketDataAPI();

    @Autowired
    public TicketDataService(AttractionWebhookClient attractionWebhookClient,
                             CacheManager cacheManager) {
        this.attractionWebhookClient = attractionWebhookClient;
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(this::loopUpdate);
    }

    private void loopUpdate() {
        updateCache();  // repopulates dataCache & evicts sortedTickets
        long delay = 4000 + random.nextInt(2602);
        Mono.delay(Duration.ofMillis(delay))
                .doOnNext(i -> loopUpdate())
                .subscribe();
    }

    private void updateCache() {
        api.pullTicketTable().forEach((key, newData) -> {
            TicketData old = dataCache.get(key);
            onUpdate(old, newData);
            dataCache.put(key, newData);
            lastUpdatedCache.put(key, System.currentTimeMillis());
        });

        // clear the sorted cache so it rebuilds on next fetch
        var cache = cacheManager.getCache("sortedTickets");
        if (cache != null) cache.clear();
    }

    private void onUpdate(TicketData oldData, TicketData newData) {
        // TODO implement any change-driven logic
    }

    public long getLastUpdated(String date) {
        return lastUpdatedCache.getOrDefault(date, 0L);
    }

    /**
     * Returns the ticket-data map sorted by date (MM-dd-yyyy),
     * caching the result until the next updateCache() run.
     * Only entries matching the pattern "MM-dd-yyyy" are included.
     */
    @Cacheable("sortedTickets")
    public Map<String, TicketData> getSortedDataCache() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        return dataCache.entrySet().stream()
                // include only properly formatted date keys
                .filter(e -> e.getKey().matches("\\d{2}-\\d{2}-\\d{4}"))
                // sort by parsed LocalDate
                .sorted(Comparator.comparing(e -> LocalDate.parse(e.getKey(), fmt)))
                // collect into linked map to preserve order
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
