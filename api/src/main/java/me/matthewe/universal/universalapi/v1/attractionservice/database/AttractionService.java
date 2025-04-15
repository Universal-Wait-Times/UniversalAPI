package me.matthewe.universal.universalapi.v1.attractionservice.database;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttractionService {
    @Getter private AttractionSnapshotRepository repository;

    @Autowired
    public AttractionService(AttractionSnapshotRepository repository) {
        this.repository = repository;
    }


}
