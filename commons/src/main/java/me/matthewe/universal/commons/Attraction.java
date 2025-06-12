package me.matthewe.universal.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attraction {


    @JsonIgnore private UUID tempId;


    @JsonProperty("park")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UniversalPark park;

    public UniversalPark getPark() {
        if (park == null) {
            switch (resortAreaCode) {
                case UOR:
                    if (venueId.contains("uor.usf")) {
                        park = UniversalPark.USF;
                    }
                    if (venueId.contains("uor.ioa")) {
                        park = UniversalPark.USF;

                    }
                    if (venueId.contains("uor.ioa")) {
                        park = UniversalPark.USF;

                    }
                    if (venueId.contains("uor.usf")) {
                        park = UniversalPark.UEU;
                    }
                    if (venueId.contains("uor.ueu")) {
                        park = UniversalPark.UEU;
                    }

                    break;
                case USJ:
                    park = UniversalPark.USJ;
                    break;
                case USH:
                    if (venueId != null) {
                        if (venueId.contains("upper_lot")) {
                            park = UniversalPark.UPPER_LOT;
                        } else {
                            park = UniversalPark.LOWER_LOT;

                        }
                    }
                    break;
            }
        }
        return park;
    }

    public void setPark(UniversalPark park) {
        this.park = park;
    }

    public UUID getTempId() {
        return tempId;
    }

    public Attraction setTempId(UUID tempId) {
        this.tempId = tempId;
        return this;
    }


    @JsonProperty("resortRegion")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ResortRegion resortRegion;


    @JsonProperty("wait_time_attraction_id")
    private String waitTimeAttractionId;

    @JsonProperty("resort_area_code")
    private ResortRegion resortAreaCode;

    @JsonProperty("venue_id")
    private String venueId;

    @JsonProperty("land_id")
    private String landId;

    @JsonProperty("closes_at")
    private String closesAt;

    @JsonProperty("name")
    private String displayName;


    @JsonProperty("category")
    private String category;

    @JsonProperty("has_single_rider")
    private boolean singleRider;

    @JsonProperty("show_externally")
    private boolean showExternally;



    @JsonProperty("modifiedAt")
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

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Queue {
        @JsonProperty("queue_id")  private String queueId;
        @JsonProperty("queue_type")  private Type queueType;
        @JsonProperty("status")  private Status status;

        @JsonProperty("display_wait_time")
        @JsonSetter(nulls = Nulls.SKIP)
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private int displayWaitTime = 0; // Default 0

        public Type getQueueType() {
            return queueType;
        }

        public Status getStatus() {
            return status;
        }

        @JsonProperty("opens_at")
        @JsonSetter(nulls = Nulls.SKIP)
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        private String opensAt;


        public int getDisplayWaitTime() {
            return displayWaitTime;
        }

        public static enum Status {
            BRIEF_DELAY, COMING_SOON, CLOSED, OPENS_AT, RIDE_NOW, OPEN,AT_CAPACITY, SPECIAL_EVENT, UNKNOWN, WEATHER_DELAY, VIRTUAL_LINE_ONLY, EXTENDED_CLOSURE, CLOSES_AT;

            @JsonCreator 
            public static Status fromString(String value) {
                try {
                    return Status.valueOf(value.toUpperCase());
                } catch (Exception e) {
                    if (value!=null&&!value.equalsIgnoreCase("N/A")){

                        System.err.println("Invalid status value: " + value);
                    }
                    return UNKNOWN; // Fallback for values like "N/A"
                }
            }
        }

        public static enum Type {
            STANDBY,
            SINGLE,EXPRESS
        }


    }




}
