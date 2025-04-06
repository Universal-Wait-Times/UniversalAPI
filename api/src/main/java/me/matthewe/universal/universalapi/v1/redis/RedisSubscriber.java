package me.matthewe.universal.universalapi.v1.redis;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisSubscriber implements MessageListener {
    @PostConstruct
    public void init() {
        System.out.println("RedisMessageListenerContainer initialized");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // Convert the message body and channel from byte[] to String.
        String channel = new String(message.getChannel());
        String msg = new String(message.getBody());
        System.out.println("Received message: " + msg + " on channel: " + channel);
        // Handle the status change here.
    }
}
