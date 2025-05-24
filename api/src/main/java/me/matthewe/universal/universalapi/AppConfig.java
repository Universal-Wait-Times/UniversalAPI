package me.matthewe.universal.universalapi;

import me.matthewe.universal.universalapi.v1.redis.RedisPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow all paths
                        .allowedOrigins("*") // Allow all origins
                        .allowedMethods("*") // Allow all HTTP methods (GET, POST, etc.)
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }
//
//    @Bean
//    public RedisPublisher redisPublisher() {
//        return new RedisPublisher();
//    }
}