package me.matthewe.universal.universalapi.utils;

import me.matthewe.universal.universalapi.v1.UniversalPark;
import me.matthewe.universal.universalapi.v1.attractionservice.Attraction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

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
    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    // Scheduler to process the queue.
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // Schedule a task to poll and send one message from the queue every second.
        scheduler.scheduleAtFixedRate(() -> {
            String message = null;
            try {
                message = messageQueue.poll();
                if (message != null) {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("content", message);

                    String discordWebhookUrl = System.getenv("DISCORD_WEBHOOK_URL");
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

                    String response = restTemplate.postForObject(discordWebhookUrl, request, String.class);
                    System.out.println("Sent queued message. Discord webhook response: " + response);
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
    public static void sendStatusUpdate(Attraction oldAttraction, Attraction attraction) {
        String discordWebhookUrl = System.getenv("DISCORD_WEBHOOK_URL");
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            System.err.println("Discord webhook URL is not configured!");
            return;
        }


        // Get the first queue (assuming at least one exists).


        String message = null;


        String resortInfo = "";

        final UniversalPark park = attraction.getPark();
        switch (attraction.getResortAreaCode()) {
            case UOR -> {
                if (park == UniversalPark.IOA) {
                    resortInfo = "Islands of Adventure";
                } else if (park == UniversalPark.USF) {
                    resortInfo = "Universal Studios Florida";
                } else if (park == UniversalPark.UEU) {
                    resortInfo = "Universal Epic Universe";
                }
            }
            case USJ -> {
                resortInfo = "Universal Studios Japan";
            }
            case USH -> {
                resortInfo = "Universal Studios Hollywood's " + park.getParkName();
            }
        }

        if ((oldAttraction.getQueues().size() > 1) && (attraction.getQueues().size() > 1)) {
            Attraction.Queue singleQueueOld = oldAttraction.getQueues().get(0);
            Attraction.Queue.Status oldSingleStatus = singleQueueOld.getStatus();

            Attraction.Queue singleQueueNew = attraction.getQueues().get(0);
            Attraction.Queue.Status newSingleStatus = singleQueueNew.getStatus();

            if (oldSingleStatus!=newSingleStatus) {

                if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.CLOSED) {
                    goMessage( String.format("%s's at %s Single Rider line is now closed.",
                            attraction.getDisplayName(),
                            resortInfo));
                } else  if (oldSingleStatus== Attraction.Queue.Status.OPEN && newSingleStatus==Attraction.Queue.Status.AT_CAPACITY) {
                    goMessage( String.format("%s's at %s Single Rider line is now at capacity.",
                            attraction.getDisplayName(),
                            resortInfo));
                }else  if (oldSingleStatus== Attraction.Queue.Status.CLOSED && newSingleStatus==Attraction.Queue.Status.OPEN) {
                    goMessage(String.format("%s's at %s Single Rider line is now open.",
                            attraction.getDisplayName(),
                            resortInfo));
                } else {
                 
                    goMessage("SINGLE RIDER DEBUG " + attraction.getDisplayName() + " ("+oldSingleStatus+"->"+newSingleStatus+")");
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
                        "experiencing a brief delay");
            }
            case WEATHER_DELAY -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "experiencing a weather delay");
                //TODO implement weather info
            }
            case CLOSED -> {
                message = String.format("%s at %s is now %s",
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
                        "walk on");
            }

            case OPEN -> {
                Attraction.Queue.Status oldStatus = oldAttraction.getQueues().get(0).getStatus();

                if (oldStatus == Attraction.Queue.Status.BRIEF_DELAY) {
                    message = String.format("%s at %s is now %s after experiencing a brief delay",
                            attraction.getDisplayName(),
                            resortInfo,
                            "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                } else if (oldStatus == Attraction.Queue.Status.WEATHER_DELAY) {
                    message = String.format("%s at %s is now %s after experiencing a weather delay",
                            attraction.getDisplayName(),
                            resortInfo,
                            "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                } else {

                    message = String.format("%s at %s is now %s",
                            attraction.getDisplayName(),
                            resortInfo,
                            "open with wait time of " + queue.getDisplayWaitTime() + " minutes");
                }
            }
            case AT_CAPACITY -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "at capacity");
            }
            case SPECIAL_EVENT -> {
                message = String.format("%s at %s is now %s",
                        attraction.getDisplayName(),
                        resortInfo,
                        "open for a special event");
            }
            case UNKNOWN -> {
                message = null;
            }
        }


        goMessage(message);
    }

    private static boolean isLocalTesting() {
        String localTesting = System.getenv("localtesting");
        if ((localTesting != null) && localTesting.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }

    private static void goMessage(String message) {
        if (message == null) return;

        if (isLocalTesting()) {
            message = "[local] " + message;
        }
        System.out.println(message);
        // Enqueue the message for throttled sending.
        messageQueue.offer(message);
        System.out.println("Queued Discord message: " + message);
    }
}
