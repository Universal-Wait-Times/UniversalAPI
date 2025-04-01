package me.matthewe.universal.universalapi;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Attraction {

    @JsonIgnore private ResortRegion resortRegion;
    @JsonProperty("wait_time_attraction_id")
    private String waitTimeAttractionId;

    @JsonProperty("resort_area_code")
    private ResortRegion resortAreaCode;

    @JsonProperty("venue_id")
    private String venueId;

    @JsonProperty("land_id")
    private String landId;

    @JsonProperty("name")
    private String displayName;


    @JsonProperty("category")
    private String category;

    @JsonProperty("has_single_rider")
    private boolean singleRider;

    @JsonProperty("show_externally")
    private boolean showExternally;



    @JsonProperty("modified_at")
    private OffsetDateTime modifiedAt;


    @JsonProperty("queues")
    private List<Queue> queues;

    public ResortRegion getResortRegion() {
        return resortRegion;
    }

    public String getWaitTimeAttractionId() {
        return waitTimeAttractionId;
    }

    public ResortRegion getResortAreaCode() {
        return resortAreaCode;
    }

    public String getVenueId() {
        return venueId;
    }

    public String getLandId() {
        return landId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isSingleRider() {
        return singleRider;
    }

    public boolean isShowExternally() {
        return showExternally;
    }

    public OffsetDateTime getModifiedAt() {
        return modifiedAt;
    }

    public List<Queue> getQueues() {
        return queues;
    }

    public static class Queue {
        @JsonProperty("queue_id")  private String queueId;
        @JsonProperty("queue_type")  private Type queueType;
        @JsonProperty("status")  private Status status;

        @JsonProperty("display_wait_time")
        @JsonSetter(nulls = Nulls.SKIP)
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)

        private int displayWaitTime = 0; // Default 0


        @JsonProperty("opens_at")
        @JsonSetter(nulls = Nulls.SKIP)
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)

        private OffsetDateTime opensAt;


        public static enum Status {
            BRIEF_DELAY, CLOSED, OPENS_AT, RIDE_NOW, OPEN, UNKNOWN;

            @JsonCreator
            public static Status fromString(String value) {
                try {
                    return Status.valueOf(value.toUpperCase());
                } catch (Exception e) {
                    return UNKNOWN; // Fallback for values like "N/A"
                }
            }
        }

        public static enum Type {
            STANDBY,
            SINGLE
        }


    }




}
