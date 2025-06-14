package me.matthewe.universal.universalapi.v1.queuetimes;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.UniversalPark;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Log
@Service
public class QueueTimesService {
    private final CacheManager cacheManager = createSimpleCacheManager();

    private CacheManager createSimpleCacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        List<ConcurrentMapCache> list = new ArrayList<>();
        for (UniversalPark value : UniversalPark.values()) {
            list.add(new ConcurrentMapCache("queueTimes-"+value.name()));
        }
        manager.setCaches(list);
        manager.initializeCaches(); // 🔐 REQUIRED to avoid NPEs


        return manager;
    }

    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(this::loopUpdate);
    }

    private void loopUpdate() {
        updateCache();  // repopulates dataCache & evicts sortedTickets
        long delay = 80000 + random.nextInt(52040);
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
        UniversalPark[] parks = UniversalPark.values();
        LocalDate now = LocalDate.now();  // correct year/month

        for (int i = 0; i < parks.length; i++) {
            UniversalPark park = parks[i];
            int delay = i * (1500 + random.nextInt(500));

            Mono.delay(Duration.ofMillis(delay))
                    .publishOn(Schedulers.boundedElastic())
                    .subscribe(ignored -> fetchParkCalendar(park, now.getYear(), now.getMonthValue()));
        }
    }


    private void fetchParkCalendar(UniversalPark park, int year, int month) {
        String yearString = String.format("%04d", year);   // e.g., 2025
        String monthString = String.format("%02d", month); // e.g., 06

        String url = String.format("https://queue-times.com/en-US/parks/%s/calendar/%s/%s",
                park.getQueueTimeId(),
                yearString,
                monthString
        );

        log.info("Pulling: " + url);

        try {
            String userAgent = userAgents.get(random.nextInt(userAgents.size()));
            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(10_000)
                    .get();

            Elements elements = doc.select(".tile.is-ancestor.is-vertical");
            log.info(elements.size() + " SIZE ");
            if (elements.isEmpty()) return;

            for (Element box : elements.get(0).select(".tile.is-child.box.is-radiusless.is-clearfix")) {
                // Crowd %
                String crowdPercent = box.select("div.tags.is-pulled-right span").first().text().trim();

                // Background color
                String style = box.attr("style");
                Color color = null;
                int r=0;
                int g=0;
                int b=0;
                if (style.contains("rgb")) {
                    String rgbString = style.substring(style.indexOf("rgb") + 4, style.indexOf(")"));
                    String[] parts = rgbString.split(",");
                    try {
                         r = Integer.parseInt(parts[0].trim());
                         g = Integer.parseInt(parts[1].trim());
                         b = Integer.parseInt(parts[2].trim());
                        color = new Color(r, g, b);
                    } catch (Exception ignored) {}
                }

                // Extract day number from left tag (e.g., "Sun 28" or just "28")
                String leftText = box.select("div.tags.is-pulled-left .tag").last().text().trim();
                int day = Integer.parseInt(leftText.replaceAll("\\D", "")); // remove non-digits

                // Assemble proper full date
                String fullDate = String.format("%04d-%02d-%02d", year, month, day);

                // Output
                System.out.println("Date: " + fullDate);
                System.out.println("Crowd: " + crowdPercent);
                if (color != null) {
                    System.out.printf("Color: rgb(%d, %d, %d)%n", color.getRed(), color.getGreen(), color.getBlue());
                }
                System.out.println("Full Text: " + box.text());
                System.out.println("----");

                cacheManager.getCache("queueTimes-"+park.name() ).put(fullDate, new QueueTimeInfo(fullDate,Integer.parseInt(crowdPercent.replaceAll("\\D+", "").trim()),r,g,b));

            }



            System.out.println("Fetched " + park.name() + " with UA: " + userAgent);
        } catch (Exception e) {
            System.err.println("Failed to fetch " + url);
            e.printStackTrace();
        }
    }


}
