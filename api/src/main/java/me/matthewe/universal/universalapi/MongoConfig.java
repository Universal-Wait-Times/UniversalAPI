package me.matthewe.universal.universalapi;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        CodecRegistry defaultRegistry = MongoClientSettings.getDefaultCodecRegistry();
        CodecRegistry jsr310Registry = CodecRegistries.fromProviders(new Jsr310CodecProvider());
        CodecRegistry combined = CodecRegistries.fromRegistries(defaultRegistry, jsr310Registry);

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
            .codecRegistry(combined)
            .build();

        return MongoClients.create(settings);
    }
}