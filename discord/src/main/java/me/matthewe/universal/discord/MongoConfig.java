package me.matthewe.universal.discord;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() {
        CodecRegistry defaultRegistry = MongoClientSettings.getDefaultCodecRegistry();
        CodecRegistry jsr310Registry = CodecRegistries.fromProviders(new Jsr310CodecProvider());
        CodecRegistry combinedRegistry = CodecRegistries.fromRegistries(defaultRegistry, jsr310Registry);

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(System.getenv("SPRING_DATA_MONGODB_URI")))
                .codecRegistry(combinedRegistry)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient) {
        return new SimpleMongoClientDatabaseFactory(mongoClient, "universalapi");
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }
}
