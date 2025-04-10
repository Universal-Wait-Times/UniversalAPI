package me.matthewe.universal.universalapi.v1.settings;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SettingRepository extends MongoRepository<Setting, String> {
}