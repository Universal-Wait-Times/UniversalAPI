package me.matthewe.universal.universalapi.v1.attractionservice.database;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttractionSnapshotRepository extends MongoRepository<AttractionSnapshot, String> {
}