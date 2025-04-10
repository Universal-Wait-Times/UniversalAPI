package me.matthewe.universal.universalapi.utils;

import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalImageSource;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.universalapi.v1.attractionservice.Attraction;
import me.micartey.webhookly.DiscordWebhook;
import me.micartey.webhookly.embeds.EmbedObject;
import me.micartey.webhookly.embeds.Footer;
import me.micartey.webhookly.embeds.Image;
import me.micartey.webhookly.embeds.Thumbnail;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordWebhookUtil {

    // Read the webhook URL from an environment variable
    private static final RestTemplate restTemplate = new RestTemplate();

    // A thread-safe queue to hold messages waiting to be sent.
    private static final BlockingQueue<DiscordWebhook> messageQueue = new LinkedBlockingQueue<>();

    // Scheduler to process the queue.
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();



    static {
        // Schedule a task to poll and send one message from the queue every second.
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
    public static void sendAttractionStatusUpdate(Attraction oldAttraction, Attraction attraction) {


        String message = null;


        String resortInfo = "";

        final UniversalPark park = attraction.getPark();
        switch (attraction.getResortAreaCode()) {
            case UOR -> {
                resortInfo=park.getParkName();
            }
            case USJ -> {
                resortInfo = "Universal Studios Japan";
            }
            case USH -> {
                resortInfo = "Universal Studios Hollywood's " + park.getParkName();
            }
        }

        if (oldAttraction!=null){

            if ((oldAttraction.getQueues().size() > 1) && (attraction.getQueues().size() > 1)) {
                Attraction.Queue singleQueueOld = oldAttraction.getQueues().get(0);
                Attraction.Queue.Status oldSingleStatus = singleQueueOld.getStatus();

                Attraction.Queue singleQueueNew = attraction.getQueues().get(0);
                Attraction.Queue.Status newSingleStatus = singleQueueNew.getStatus();

                if (oldSingleStatus!=newSingleStatus) {

                    if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s's at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }   else if (oldSingleStatus== Attraction.Queue.Status.WEATHER_DELAY && newSingleStatus==Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s's at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }   else if (oldSingleStatus== Attraction.Queue.Status.CLOSED && newSingleStatus==Attraction.Queue.Status.OPENS_AT) {
                       //No msg
                    }   else if (oldSingleStatus== Attraction.Queue.Status.BRIEF_DELAY && newSingleStatus==Attraction.Queue.Status.CLOSED) {
                        goMessage(oldAttraction, attraction, String.format("%s's at %s single rider line is now closed.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }   else if (oldSingleStatus== Attraction.Queue.Status.BRIEF_DELAY && newSingleStatus==Attraction.Queue.Status.OPEN) {
                        goMessage(oldAttraction, attraction, String.format("%s's at %s single rider line is now open after experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo));
                    } else  if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.AT_CAPACITY) {
                        goMessage(  oldAttraction,attraction,String.format("%s's at %s single rider line is now at capacity.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }else  if (oldSingleStatus== Attraction.Queue.Status.CLOSED && newSingleStatus==Attraction.Queue.Status.OPEN) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider line is now open.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }else  if (oldSingleStatus== Attraction.Queue.Status.OPENS_AT && newSingleStatus==Attraction.Queue.Status.WEATHER_DELAY) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider will open late due to a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }else  if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.WEATHER_DELAY) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider is experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }else  if (oldSingleStatus== Attraction.Queue.Status.OPENS_AT && newSingleStatus==Attraction.Queue.Status.BRIEF_DELAY) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider will open late due to a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo));
                    }else  if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.BRIEF_DELAY) {
                        goMessage(oldAttraction, attraction, String.format("%s's at %s single rider is experiencing a brief delay.",
                                attraction.getDisplayName(),
                                resortInfo));

                    } else  if (oldSingleStatus== Attraction.Queue.Status.OPENS_AT && newSingleStatus==Attraction.Queue.Status.OPEN) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider line is now open.", //TODO check if on schedule
                                attraction.getDisplayName(),
                                resortInfo));
                    }  else  if (oldSingleStatus== Attraction.Queue.Status.WEATHER_DELAY && newSingleStatus==Attraction.Queue.Status.OPEN) {
                        goMessage( oldAttraction,attraction,String.format("%s's at %s single rider line is now open after experiencing a weather delay.",
                                attraction.getDisplayName(),
                                resortInfo));
                    } else {

                        goMessage( oldAttraction,attraction,"SINGLE RIDER DEBUG " + attraction.getDisplayName() + " ("+oldSingleStatus+"->"+newSingleStatus+")");
                    }
                }
            }
        }

        Attraction.Queue queue = attraction.getQueues().get(0);

        Attraction.Queue.Status status = queue.getStatus();
        switch (status) {
            case BRIEF_DELAY -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "experiencing a brief delay.");
            }
            case WEATHER_DELAY -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "experiencing a weather delay.");
                //TODO implement weather info
            }
            case CLOSED -> {
                message = String.format("%s at %s is now %s.",
                        attraction.getDisplayName(),
                        resortInfo,
                        "closed");
            }
            case OPENS_AT -> {
                message = null;
            }
            case RIDE_NOW -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "walk on.");
            }

            case OPEN -> {
                if (oldAttraction!=null) {

                    Attraction.Queue.Status oldStatus = oldAttraction.getQueues().get(0).getStatus();

                    if (oldStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                        message = String.format("%s at %s is now %s after experiencing a brief delay",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes.");
                    } else if (oldStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                        message = String.format("%s at %s is now %s after experiencing a weather delay",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes.");
                    } else {

                        message = String.format("%s at %s is now %s",
                                attraction.getDisplayName(),
                                resortInfo,
                                "open with wait time of " + queue.getDisplayWaitTime() + " minutes.");
                    }
                } else {
                    message = String.format("%s at %s is now %s",
                            attraction.getDisplayName(),
                            resortInfo,
                            "open with wait time of " + queue.getDisplayWaitTime() + " minutes.");
                }
            }
            case AT_CAPACITY -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "at capacity.");
            }
            case SPECIAL_EVENT -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "open for a special event.");
            }
            case UNKNOWN -> {
                message = null;
            }
        }


        goMessage( oldAttraction,attraction,message);
    }

    private static boolean isLocalTesting() {
        String localTesting = System.getenv("localtesting");
        if ((localTesting != null) && localTesting.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param oldAttraction may be null
     * @param attraction never is null
     * @param message string msg to send
     */
    private static void goMessage(Attraction oldAttraction, Attraction attraction, String message) {
        if (message == null) return;

        if (isLocalTesting()) {
            message = "[local] " + message;
        }
//        if (attraction.getResortAreaCode()!= ResortRegion.UOR)return;//ONLY HANDLING ORLANDO CURRENTLY.
        System.out.println(message);
        DiscordWebhook webhook = null;
        if (System.getenv("DISCORD_WEBHOOK_URL")!=null){

            webhook = new DiscordWebhook(System.getenv("DISCORD_WEBHOOK_URL"));
        }


        webhook.setAvatarUrl(attraction.getPark().getLogoSource());

        Color attractionColor = Color.green;

        switch (attraction.getQueues().get(0).getStatus()){
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
                .setTimestamp(attraction.getModifiedAt())
                .setFooter(new Footer(attraction.getPark().getParkName(), attraction.getPark().getLogoSource()))
                .setDescription(message);


        webhook.getEmbeds().add(embed);

        webhook.setUsername(attraction.getPark().getParkName());
        // Enqueue the message for throttled sending.

        if (attraction.getQueues().get(0).getStatus()== Attraction.Queue.Status.RIDE_NOW) {
            webhook.setContent("@everyone");
        }

        boolean offer = messageQueue.offer(webhook);
    }
}
