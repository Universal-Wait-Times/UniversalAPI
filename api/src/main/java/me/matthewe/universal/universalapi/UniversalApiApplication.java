package me.matthewe.universal.universalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableCaching
public class UniversalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniversalApiApplication.class, args);
    }
    @Bean
    public CacheManager cacheManager() {
        // names must match what you use in @Cacheable(...)
        return new ConcurrentMapCacheManager("sortedTickets");
    }
}
