package me.matthewe.universal.discord.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;

import me.matthewe.universal.commons.virtualline.VirtualLine;
import me.matthewe.universal.commons.virtualline.VirtualLineStatus;
import me.matthewe.universal.commons.weather.WeatherData;
import me.matthewe.universal.discord.weather.WeatherService;
import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import me.micartey.webhookly.embeds.Field;
import me.micartey.webhookly.embeds.Footer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Log
@Service
public class DiscordWebhookService {

    // Read the webhook URL from an environment variable
    private final RestTemplate restTemplate = new RestTemplate();

    // A thread-safe queue to hold messages waiting to be sent.
    private final BlockingQueue<DiscordWebhook> messageQueue = new LinkedBlockingQueue<>();

    // Scheduler to process the queue.
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final WeatherService weatherService;

    @Autowired
    public DiscordWebhookService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostConstruct
    public void start() throws Exception {
        scheduler.scheduleAtFixedRate(() -> {
            DiscordWebhook message = null;
            try {
                message = messageQueue.poll();
                if (message != null) {

                    message.execute();
                }
            } catch (Exception e) {
                if (message != null) {

                    messageQueue.add(message);
                }
                System.err.println("Error sending queued Discord webhook: " + e.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * Enqueues a status update message to be sent to the Discord webhook.
     * Message format: "%s at %s is now %s"
     *
     * @param attraction the attraction object containing display name, park name, and queue status.
     */
    public void sendAttractionStatusUpdate(Attraction oldAttraction, Attraction attraction) {


        String message = null;


        String resortInfo = "";

        final UniversalPark park = attraction.getPark();
        switch (attraction.getResortAreaCode()) {
            case UOR -> {
                resortInfo = park.getParkName();
            }
            case USJ -> {
                resortInfo = "Universal Studios Japan";
            }
            case USH -> {
                resortInfo = "Universal Studios Hollywood " + park.getParkName();
            }
        }

        if (oldAttraction != null) {

            if ((oldAttraction.getQueues().size() > 1) && (attraction.getQueues().size() > 1)) {
                Attraction.Queue singleQueueOld = oldAttraction.getQueues().get(0);
                Attraction.Queue.Status oldSingleStatus = singleQueueOld.getStatus();

                Attraction.Queue singleQueueNew = attraction.getQueues().get(0);
                Attraction.Queue.Status newSingleStatus = singleQueueNew.getStatus();

                if (oldSingleStatus != newSingleStatus) {

                    if (oldSingleStatus == Attraction.Queue.Status.OPEN && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.WEATHER_DELAY && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.CLOSED && newSingleStatus == Attraction.Queue.Status.VIRTUAL_LINE_ONLY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider virtual line is now open.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.CLOSED && newSingleStatus == Attraction.Queue.Status.OPENS_AT) {
                        //No msg
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPENS_AT && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        //No msgOPENS_AT->CLOSED
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPEN && newSingleStatus == Attraction.Queue.Status.OPENS_AT) {
                        //No msg
                    } else if (oldSingleStatus == Attraction.Queue.Status.BRIEF_DELAY && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.BRIEF_DELAY && newSingleStatus == Attraction.Queue.Status.OPEN) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now open after experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPEN && newSingleStatus == Attraction.Queue.Status.AT_CAPACITY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now at capacity.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.CLOSED && newSingleStatus == Attraction.Queue.Status.OPEN) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now open.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPENS_AT && newSingleStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider will open late due to a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);

                    } else if (oldSingleStatus == Attraction.Queue.Status.CLOSED && newSingleStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPEN && newSingleStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider is experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPENS_AT && newSingleStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider will open late due to a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                        //VIRTUAL_LINE_ONLY
                    } else if (oldSingleStatus == Attraction.Queue.Status.VIRTUAL_LINE_ONLY && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.AT_CAPACITY && newSingleStatus == Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);

                    } else if (oldSingleStatus == Attraction.Queue.Status.WEATHER_DELAY && newSingleStatus == Attraction.Queue.Status.BRIEF_DELAY) {

                    } else if (oldSingleStatus == Attraction.Queue.Status.CLOSED && newSingleStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider is experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.OPEN && newSingleStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider is experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);

                    } else if (oldSingleStatus == Attraction.Queue.Status.OPENS_AT && newSingleStatus == Attraction.Queue.Status.OPEN) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now open.", //TODO check if on schedule
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else if (oldSingleStatus == Attraction.Queue.Status.WEATHER_DELAY && newSingleStatus == Attraction.Queue.Status.OPEN) {
                        goMessage(oldAttraction, attraction, String.format("%s at %s single rider line is now open after experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo), MessageType.ATTRACTION);
                    } else {

                        goMessage(oldAttraction, attraction, "SINGLE RIDER DEBUG " + attraction.getDisplayName() + " (" + oldSingleStatus + "->" + newSingleStatus + ")", MessageType.ATTRACTION);
                    }
                }
            }
        }

        Attraction.Queue queue = attraction.getQueues().get(0);

        Attraction.Queue.Status status = queue.getStatus();
        switch (status) {
            case BRIEF_DELAY -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "experiencing a brief delay");
            }

            case VIRTUAL_LINE_ONLY -> {
                message = String.format("%s at %s now has virtual line enabled!",
                        attraction.getDisplayName(),
                        resortInfo);
            }
            case WEATHER_DELAY -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "experiencing a weather delay");
                //TODO implement weather info
            }
            case CLOSED -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "closed");
            }
            case EXTENDED_CLOSURE -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "facing an extended closure");
            }
            case OPENS_AT -> {
                message = null;
            }
            case RIDE_NOW -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "walk on");
            }

            case OPEN -> {
                if (oldAttraction != null) {

                    Attraction.Queue.Status oldStatus = oldAttraction.getQueues().get(0).getStatus();

                    if (queue.getDisplayWaitTime() > 500) return; // Do not display broken wait times
                    if (oldStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                        message = String.format("%s at %s is now %s after experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                    } else if (oldStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                        message = String.format("%s at %s is now %s after experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                    } else {

                        message = String.format("%s at %s is now %s.",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                    }
                } else {
                    message = String.format("%s at %s is now %s.",
                            attraction.getDisplayName(),
                            resortInfo,
                            "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                }
            }
            case AT_CAPACITY -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "at capacity");
            }
            case SPECIAL_EVENT -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "open for a special event");
            }
            case UNKNOWN -> {
                message = null;
            }
        }


        goMessage(oldAttraction, attraction, message, MessageType.ATTRACTION);
    }

    private boolean isLocalTesting() {
        String localTesting = System.getenv("localtesting");
        if ((localTesting != null) && localTesting.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }


    public List<DiscordWebhook> getWebHookList(Attraction attraction) {

        List<DiscordWebhook> discordWebhooks = new ArrayList<>();

        DiscordWebhook webhook = null;

        switch (attraction.getResortAreaCode()) {
            case UOR -> {
                if (System.getenv("DISCORD_ORLANDO_WEBHOOK_URL") != null) {

                    webhook = new DiscordWebhook(System.getenv("DISCORD_ORLANDO_WEBHOOK_URL"));
                }
            }
            case USJ -> {
                if (System.getenv("DISCORD_JAPAN_WEBHOOK_URL") != null) {

                    webhook = new DiscordWebhook(System.getenv("DISCORD_JAPAN_WEBHOOK_URL"));
                }
            }
            case USH -> {
                if (System.getenv("DISCORD_HOLLYWOOD_WEBHOOK_URL") != null) {

                    webhook = new DiscordWebhook(System.getenv("DISCORD_HOLLYWOOD_WEBHOOK_URL"));
                }
            }
        }


        webhook.setAvatarUrl(attraction.getPark().getLogoSource());
        discordWebhooks.add(webhook);

        return discordWebhooks;


    }


    public enum MessageType {
        ATTRACTION, VIRTUAL_LINE
    }

    public void goVirtualLineMessage(VirtualLine oldVirtualLine, VirtualLine virtualLine ) {
        boolean send = false;
        VirtualLineStatus virtualLineStatus = virtualLine.getStatus();
        if (oldVirtualLine==null) {

            send =true;

        } else {
            if (!virtualLineStatus.equals(oldVirtualLine.getStatus())) {
                send=true;
            }

        }
        if (!send)return;


        List<DiscordWebhook> webHookList = getWebHookList(virtualLine);




        for (DiscordWebhook webhook : webHookList) {






            EmbedObject embed = new EmbedObject()
                    .setTitle(virtualLine.getName())
                    .setColor(virtualLineStatus.getColor())
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter(new Footer(UniversalPark.UEU.getParkName(), UniversalPark.UEU.getLogoSource())) //For now since nowhere else uses virtual lines
                    .setDescription("Virtual line is now " + virtualLineStatus.getName().toLowerCase( ) +".");




            webhook.getEmbeds().add(embed);


            if (virtualLineStatus ==VirtualLineStatus.OPEN_AVAILABLE) {
                webhook.setContent("<@&1363206630321422466>");
            } else {
                webhook.setContent(" ");
            }

            boolean offer = messageQueue.offer(webhook);
        }

    }

    private List<DiscordWebhook> getWebHookList(VirtualLine virtualLine) {
        List<DiscordWebhook> discordWebhooks = new ArrayList<>();

        List<String> urls = new ArrayList<>();

        String universalEndpointVirtualQueue = System.getenv("VIRTUAL_LINE_HP_WEBHOOKS");

        if (universalEndpointVirtualQueue.contains("@")){
            for (String s : universalEndpointVirtualQueue.split("@")) {
                urls.add(s.trim());
            }
        } else {
            urls.add(universalEndpointVirtualQueue.trim());
        }

        for (String url : urls) {
            DiscordWebhook discordWebhook = new DiscordWebhook(url);
            discordWebhook.setUsername("Virtual Line Updates");
            discordWebhook.setAvatarUrl("https://i.imgur.com/jPvBkcc.png");
            discordWebhooks.add(discordWebhook);
        }
        return discordWebhooks;

    }





    public void goAttractionMessage(Attraction oldAttraction, Attraction attraction, String message) {
        List<DiscordWebhook> webHookList = getWebHookList(attraction);
        if (attraction.getResortAreaCode() == ResortRegion.UOR && attraction.getPark() == UniversalPark.UEU) {
            DiscordWebhook discordWebhook = new DiscordWebhook(System.getenv("ADDITIONAL_EPIC_WEBHOOK"));
            discordWebhook.setAvatarUrl(attraction.getPark().getLogoSource());
            webHookList.add(discordWebhook);
            log.severe("EPIC HOOK");
        }
//        if (attraction.getResortAreaCode()!= ResortRegion.UOR)return;//ONLY HANDLING ORLANDO CURRENTLY.
        log.info("[" + webHookList.size() + "] " + message);


        for (DiscordWebhook webhook : webHookList) {
            Color attractionColor = Color.green;

            switch (attraction.getQueues().get(0).getStatus()) {
                case BRIEF_DELAY -> {
                    attractionColor = Color.YELLOW;
                }
                case CLOSED -> {
                    attractionColor = Color.RED;
                }
                case OPENS_AT -> {
                    attractionColor = Color.YELLOW;
                }
                case RIDE_NOW -> {
                    attractionColor = Color.GREEN;
                }
                case OPEN -> {
                    attractionColor = Color.GREEN;
                }
                case AT_CAPACITY -> {
                    attractionColor = Color.red.darker();
                }
                case SPECIAL_EVENT -> {
                }
                case UNKNOWN -> {
                    attractionColor = Color.gray.darker();
                }
                case WEATHER_DELAY -> {
                    attractionColor = Color.cyan.darker();
                }
            }
            EmbedObject embed = new EmbedObject()
                    .setTitle(attraction.getDisplayName())
                    .setColor(attractionColor)
                    .setTimestamp(OffsetDateTime.now())
                    .setFooter(new Footer(attraction.getPark().getParkName(), attraction.getPark().getLogoSource()))
                    .setDescription(message);
            if (attraction.getQueues().get(0).getStatus() == Attraction.Queue.Status.WEATHER_DELAY) {
                WeatherData weatherData = weatherService.getWeather(attraction.getPark());
                if (weatherData != null) {

                    String emoji = weatherData.getWeatherEmoji();

                    StringBuilder weatherInfo = new StringBuilder();
                    weatherInfo.append(String.format("%s %.1f℉\n", emoji, weatherData.getTemperature()));
                    weatherInfo.append(String.format("%d/mph wind (gusts %d/mph)", (int) weatherData.getWindSpeed(), (int) weatherData.getWindGusts()));

                    if (weatherData.getPrecipitationProbability() >= 0) {
                        weatherInfo.append(String.format("\n%.2f in rain (%d%% chance)",
                                weatherData.getPrecipitation(),
                                (int) weatherData.getPrecipitationProbability()
                        ));
                    }
                    embed.getFields().add(new Field("Weather", weatherInfo.toString(), true));
                }


            }


            webhook.getEmbeds().add(embed);

            webhook.setUsername(attraction.getPark().getParkName());
            // Enqueue the message for throttled sending.

            if (attraction.getQueues().get(0).getStatus() == Attraction.Queue.Status.RIDE_NOW) {
                webhook.setContent("@everyone");
            }

            boolean offer = messageQueue.offer(webhook);

        }
    }

    public void sendVirtualLineStatusUpdate(VirtualLine oldVirtualLine, VirtualLine virtualLine) {

        goVirtualLineMessage(oldVirtualLine, virtualLine);
    }


    /**
     * @param oldAttraction may be null
     * @param attraction    never is null
     * @param message       string msg to send
     */
    private void goMessage(Object oldAttraction, Object attraction, String message, MessageType messageType) {
        if (message == null) return;

        if (isLocalTesting()) {
            message = "[local] " + message;
        }
        if (messageType==MessageType.ATTRACTION){
            goAttractionMessage((Attraction) oldAttraction, (Attraction) attraction,message);
        }

    }

}

