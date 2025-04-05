package me.matthewe.universal.universalapi;

import me.matthewe.universal.universalapi.v1.redis.RedisPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

//
//    @Bean
//    public RedisPublisher redisPublisher() {
//        return new RedisPublisher();
//    }
}