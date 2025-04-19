package me.matthewe.universal.universalapi.v1.venue;


import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import me.matthewe.universal.universalapi.v1.AttractionWebhookClient;
import me.matthewe.universal.universalapi.v1.virtualline.VirtualLineResponse;
import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import me.micartey.webhookly.embeds.Footer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Log
@Service
public class VenueService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl(System.getenv("UNIVERSAL_ENDPOINT_VIRTUAL_QUEUE"))
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10 MB
                    .build())
            .build();

    private AttractionWebhookClient attractionWebhookClient;
    private final Map<String, Map<String, Venue>> dataCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdatedCache = new ConcurrentHashMap<>();
    private final Random random = new Random();


    @PostConstruct
    public void startUpdater() {
        Schedulers.parallel().schedule(() -> loopUpdate("Orlando"));
        Schedulers.parallel().schedule(() -> loopUpdate("Hollywood"));
    }


    private void updateCache(String city) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/Venues")
                        .queryParam("city", city)
                        .queryParam("pageSize", "All")
                        .build())
                .retrieve()
                .bodyToMono(VenueData.class)
                .map(VenueData::getVenus)
                .doOnError(e -> log.warning("❌ Failed to update cache for " + city + ": " + e.getMessage()))
                .subscribe(newData -> {
                    Map<String, Venue> oldData = null;
                    if (dataCache.containsKey(city)) {

                        oldData = dataCache.get(city);

                    }
                    Map<String, Venue> newDatas =new HashMap<>();
                    for (Venue newDatum : newData) {
                        newDatas.put(newDatum.getDisplayName(), newDatum);
                    }
                    dataCache.put(city, newDatas);
                    lastUpdatedCache.put(city, System.currentTimeMillis());
                    try {
                        onUpdate(city, oldData, newDatas);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void onUpdate(String city, Map<String, Venue> oldData, Map<String, Venue> newData) throws IOException {


        if (!city.equalsIgnoreCase("Orlando")) {
            return;
        }


        checkEpicHoursAdded( oldData, newData);

    }

    private void checkEpicHoursAdded( Map<String, Venue> oldData,  Map<String, Venue> newData) throws IOException {
        if (oldData==null)return;

        for (Venue oldDatum : oldData.values()) {

            if (!oldDatum.getDisplayName().equalsIgnoreCase("Universals Epic Universe")) continue;

            for (Venue venue : newData.values()) {
                if (!venue.getDisplayName().equalsIgnoreCase("Universals Epic Universe")) continue;


                //FOUND EPIC;
                if (oldDatum.getHours()==null||oldDatum.getHours().isEmpty()) {
                    if (venue.getHours()!=null&&!venue.getHours().isEmpty()) {
                        logEpicHoursAddedFinally();
                    }
                }
            }
        }
    }

    private void logEpicHoursAddedFinally() throws IOException {

        DiscordWebhook discordWebhook =new DiscordWebhook(System.getenv("ADDITIONAL_EPIC_WEBHOOK"));
        discordWebhook.setUsername("Epic Hours");
        discordWebhook.setContent("<@158445315412852736>");
        discordWebhook.setAvatarUrl(UniversalPark.UEU.getLogoSource());

        EmbedObject embedObject =new EmbedObject()
                .setTimestamp(OffsetDateTime.now())
                .setColor(Color.YELLOW.darker().darker())
                .setFooter(new Footer(UniversalPark.UEU.getParkName(), UniversalPark.UEU.getLogoSource()))
                .setDescription("EPIC HOURS RELEASED");
        discordWebhook.getEmbeds().add(embedObject);
        discordWebhook.execute();
    }

    private void loopUpdate(String city) {
        updateCache(city);
        int randomMinutes = 20 + random.nextInt(16); // 20 to 35 minutes
        Mono.delay(Duration.ofMinutes(randomMinutes))
                .doOnNext(i -> loopUpdate(city))
                .subscribe();
    }

    public  Map<String, Venue> get(ResortRegion region) {
        switch (region) {
            case UOR -> {
                return dataCache.get("Orlando");
            }
            case USJ -> {
                return null;
            }
            case USH -> {
                return dataCache.get("Hollywood");
            }
        }
        return null;
    }
}
