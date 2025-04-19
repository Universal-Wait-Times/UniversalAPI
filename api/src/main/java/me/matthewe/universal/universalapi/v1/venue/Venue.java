package me.matthewe.universal.universalapi.v1.venue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class Venue {
    @JsonProperty("MblDisplayName")
    private String displayName;


    @JsonProperty("ContainedLands")
    private List<Land> lands;

    @JsonProperty("StreetAddress")
    private StreetAddress streetAddress;

    @JsonProperty("GpsBoundary")
    private List<GpsBoundary> gpsBoundaries;

    @JsonProperty("GpsBoundaryCircle")
    private GpsBoundaryCircle gpsBoundaryCircle;


    @JsonProperty("Hours")
    private List<Hours> hours;



    @JsonProperty("AdmissionRequired")
    private boolean admissionRequired;

    @JsonProperty("Latitude")  private double latitude;
    @JsonProperty("Longitude")  private double longitude;
    @JsonProperty("Color")  private String color;
    @JsonProperty("VenueType")  private String venueType;


    @Data
    public static class Hours {
        @JsonProperty("Date")  private Date date;
        @JsonProperty("OpenTimeString")  private String openTimeString;
        @JsonProperty("CloseTimeString")  private String closeTimeString;
        @JsonProperty("OpenTimeUnix")  private long openTimeUnix;
        @JsonProperty("CloseTimeUnix")  private long closeTimeUnix;
    }
    @Data
    public static class GpsBoundaryCircle {

        @JsonProperty("RadiusInMeters")  private double radiusInMeters;
        @JsonProperty("Latitude")  private double latitude;
        @JsonProperty("Longitude")  private double longitude;
    }

    @Data
    public static class StreetAddress {
        @JsonProperty("AddressLine1")  private String addressLine1;
        @JsonProperty("City")  private String city;
        @JsonProperty("State")  private String state;
        @JsonProperty("ZipCode")  private String zipCode;

    }

    @Data
    public static class GpsBoundary {
        @JsonProperty("Latitude")  private double latitude;
        @JsonProperty("Longitude")  private double longitude;


    }

    @Data
    public static class Land {
        @JsonProperty("MblDisplayName")
        private String displayName;
        @JsonProperty("ContainingVenueId")
        private int containingVenueId;

        @JsonProperty("Id")
        private int id;

        @JsonProperty("Longitude")
        private double longitude;

        @JsonProperty("Latitude")
        private double latitude;

        @JsonProperty("ExternalIds")
        private Map<String, String> externalIds;


    }
}
