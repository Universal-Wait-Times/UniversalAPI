package me.matthewe.universal.universalapi.v1.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;


@Service
public class RedisPublisher {

    private final StringRedisTemplate template;

    // A thread-safe queue to cache messages if needed
    private final Queue<RedisMessage> messageCache = new LinkedList<>();

    public RedisPublisher(StringRedisTemplate template) {
        this.template = template;
    }

    /**
     * Publishes a message to the given channel.
     * This method will block until Redis is connected.
     */
    public synchronized void publish(String channel, String message) {
        // Wait until Redis is connected
        while (!isRedisConnected()) {
            try {
                System.out.println("Redis not connected. Waiting to publish message...");
                // Wait for 1 second and check again.
                wait(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Publish interrupted");
                return;
            }
        }

        // Once connected, flush any cached messages
        flushCache();

        // Now publish the current message
        try {
            template.convertAndSend(channel, message);
            System.out.println("Published message on channel " + channel);
        } catch (Exception e) {
            System.err.println("Error publishing message: " + e.getMessage());
            // Optionally cache the message if publishing fails unexpectedly.
            messageCache.add(new RedisMessage(channel, message));
        }
    }

    /**
     * Scheduled task that periodically flushes any cached messages when Redis is connected.
     */
    @Scheduled(fixedDelay = 5000)
    public synchronized void flushCache() {
        if (!isRedisConnected()) {
            return;
        }
        // Notify any threads waiting in publish() that Redis is now connected.
        notifyAll();
        while (!messageCache.isEmpty()) {
            RedisMessage msg = messageCache.poll();
            try {
                template.convertAndSend(msg.channel, msg.message);
                System.out.println("Flushed cached message on channel " + msg.channel);
            } catch (Exception e) {
                System.err.println("Error flushing cached message: " + e.getMessage());
                // If sending fails, put the message back and exit
                messageCache.add(msg);
                break;
            }
        }
    }

    /**
     * Checks if Redis is connected by sending a PING.
     */
    private boolean isRedisConnected() {
//        try {
//            String pong = template.getConnectionFactory().getConnection().ping();
//            return "PONG".equalsIgnoreCase(pong);
//        } catch (Exception e) {
//            return false;
//        }
        return RedisConfig.isRedisSubscribed();
    }

    /**
     * Simple helper class to hold a channel/message pair.
     */
    private static class RedisMessage {
        final String channel;
        final String message;

        RedisMessage(String channel, String message) {
            this.channel = channel;
            this.message = message;
        }
    }
}
