package me.matthewe.universal.universalapi.v1.queuetimes;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.commons.ticketdata.TicketData;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import me.matthewe.universal.universalapi.v1.hours.ParkHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Random;

@Log
@Service
public class QueueTimesService {
    private final CacheManager cacheManager;
    @Autowired
    public QueueTimesService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(this::loopUpdate);
    }

    private void loopUpdate() {
        updateCache();  // repopulates dataCache & evicts sortedTickets
        long delay = 80000 + new Random().nextInt(52040);
        Mono.delay(Duration.ofMillis(delay))
                .doOnNext(i -> loopUpdate())
                .subscribe();
    }

    private void updateCache() {
        for (UniversalPark value : UniversalPark.values()) {
            String url = String.format("https://queue-times.com/en-US/parks/%s/calendar", value.getQueueTimeId());

        }
    }

}
