package me.matthewe.universal.universalapi.v1.attractionservice.database;

public interface AttractionSnapshotRepository extends MongoRepository<AttractionSnapshot, String> {
    AttractionSnapshot findTopByWaitTimeAttractionIdOrderByPulledAtDesc(String id);
}