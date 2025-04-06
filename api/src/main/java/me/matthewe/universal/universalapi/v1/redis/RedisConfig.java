package me.matthewe.universal.universalapi.v1.redis;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

//    private final RedisConnectionFactory connectionFactory;
//    private final RedisSubscriber redisSubscriber;
//    private RedisMessageListenerContainer container;
//
//    public RedisConfig(RedisConnectionFactory connectionFactory, RedisSubscriber redisSubscriber) {
//        this.connectionFactory = connectionFactory;
//        this.redisSubscriber = redisSubscriber;
//    }
//
//    @Bean
//    public RedisMessageListenerContainer container() {
//        container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//        container.addMessageListener(redisSubscriber, new PatternTopic("ride-status-update"));
//        // Ensure the container starts automatically
////        container.setAutoStartup(true);
//        return container;
//    }
//
//    private static boolean redisSubscribed = false;
//
//    public static boolean isRedisSubscribed() {
//        return redisSubscribed;
//    }
//
//    @PostConstruct
//    public void startContainerImmediately() {
//        // Force the container to initialize and start immediately.
//        if (container != null) {
//            container.afterPropertiesSet();
//            container.start();
//            System.out.println("RedisMessageListenerContainer started synchronously");
//            redisSubscribed=true;
//        }
//    }
}
