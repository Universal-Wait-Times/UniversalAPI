package me.matthewe.universal.universalapi.v1.show;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Show {
    @JsonProperty("show_id")
    private String showId;


    @JsonProperty("resort_area_code")
    private String resortAreaCode;

    @JsonProperty("venue_id")
    private String venueId;
    @JsonProperty("land_id")
    private String landId;

    @JsonProperty("show_type")
    private ShowType showType;


    @JsonProperty("category")
    private String category;

    @JsonProperty("status")
    private ShowStatus showStatus;


    @JsonProperty("show_externally")
    private boolean showExternally;

    @JsonProperty("fixed_time")
    private String fixedTime;

    @JsonProperty("modified_at")
    private String modifiedAt;


    @JsonProperty("next_show_time_id")
    private String nextShowTimeId;

    @JsonProperty("aap_access_code")
    private String aapAccessCode;

    @JsonProperty("aap_plu")
    private String aapPlu;


    public static enum ShowType {
        CHARACTER_APPEARANCE, SHOW, UNKNOWN;
    }

    public static enum ShowTimesStatus {
        ENABLED,CANCELED,AT_CAPACITY
    }

    public static enum ShowStatus {
        CLOSED, OPEN;
    }

    public static class ShowTime {
        @JsonProperty("show_time_id")
        private String showTimeId;
        @JsonProperty("status")
        private ShowTimesStatus showTimesStatus;
        @JsonProperty("start_time")
        private String startTime;
        @JsonProperty("asl")
        private boolean asl;


    }

}