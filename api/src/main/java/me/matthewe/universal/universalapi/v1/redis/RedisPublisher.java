package me.matthewe.universal.universalapi.v1.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    private final StringRedisTemplate template;

    public RedisPublisher(StringRedisTemplate template) {
        this.template = template;
    }

    public void publish(String channel, String message) {
        template.convertAndSend(channel, message);
    }
}