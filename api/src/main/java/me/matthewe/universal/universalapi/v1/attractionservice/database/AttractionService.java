package me.matthewe.universal.universalapi.v1.attractionservice.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AttractionService {
    private AttractionSnapshotRepository attractionSnapshotRepository;

    @Autowired
    public AttractionService(AttractionSnapshotRepository attractionSnapshotRepository) {
        this.attractionSnapshotRepository = attractionSnapshotRepository;
    }
}
