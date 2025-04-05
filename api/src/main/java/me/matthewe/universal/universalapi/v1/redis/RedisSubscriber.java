package me.matthewe.universal.universalapi.v1.redis;

import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber {

    public void onMessage(String message, String channel) {
        System.out.println("Received message: " + message + " on channel: " + channel);
        // handle the status change here
    }
}
