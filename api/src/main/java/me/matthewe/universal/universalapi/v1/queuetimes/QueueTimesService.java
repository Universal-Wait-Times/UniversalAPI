package me.matthewe.universal.universalapi.v1.queuetimes;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.commons.ticketdata.TicketData;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import me.matthewe.universal.universalapi.v1.hours.ParkHoursService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
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
        long delay = 80000 +random.nextInt(52040);
        Mono.delay(Duration.ofMillis(delay))
                .doOnNext(i -> loopUpdate())
                .subscribe();

    }

    private final List<String> userAgents = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.1 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1"
    );

    private final Random random = new Random();

    public void updateCache() {

        for (UniversalPark value : UniversalPark.values()) {
            String url = String.format("https://queue-times.com/en-US/parks/%s/calendar", value.getQueueTimeId());

            try {
                String userAgent = userAgents.get(random.nextInt(userAgents.size()));
                Document doc = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .timeout(10_000)
                        .get();

                // Optional: Print or cache result
                System.err.println(doc);
                System.out.println("Fetched " + value.name() + " with UA: " + userAgent);
                // Example: Extract something (e.g., calendar dates)
                // Elements days = doc.select(".calendar-day"); // Adjust as needed

            } catch (Exception e) {
                System.err.println("Failed to fetch " + url);
                e.printStackTrace();
            }
        }
    }

}
