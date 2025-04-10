package me.matthewe.universal.universalapi.v1.attractionservice.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;
import java.util.List;

@Document(collection = "attractions_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionSnapshot {
    @Id
    private String waitTimeAttractionId;

    private String displayName;
    private OffsetDateTime modifiedAt;
    private OffsetDateTime pulledAt;

    private UniversalPark park;
    private ResortRegion resort;

    private String venueId;
    private String landId;
    private List<Attraction.Queue> queues;

    public static AttractionSnapshot from(Attraction attraction) {
        AttractionSnapshot snapshot = new AttractionSnapshot();
        snapshot.waitTimeAttractionId = attraction.getWaitTimeAttractionId();
        snapshot.displayName = attraction.getDisplayName();
        snapshot.modifiedAt = attraction.getModifiedAt();
        snapshot.pulledAt = OffsetDateTime.now();
        snapshot.park = attraction.getPark();
        snapshot.resort = attraction.getResortAreaCode();
        snapshot.venueId = attraction.getVenueId();
        snapshot.landId = attraction.getLandId();
        snapshot.queues = attraction.getQueues();
        return snapshot;
    }


    // Add queues as a List<QueueSnapshot> if needed
}