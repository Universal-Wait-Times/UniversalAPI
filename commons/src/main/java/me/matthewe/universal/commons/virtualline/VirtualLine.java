package me.matthewe.universal.commons.virtualline;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualLine {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("PlaceId")
    private String placeId;


    @JsonProperty("Name")
    private String name;

    @JsonProperty("MaxAppointmentSize")
    private int maxAppointmentSize;

    @JsonProperty("AppointmentDuration")
    private String appointmentDuration;

    @JsonProperty("SessionTimeoutInSec")
    private int sessionTimeoutInSec;

    @JsonProperty("QueueEntityId")
    private int queueEntityId;


    @JsonProperty("QueueEntityType")
    private String queueEntityType;


    @JsonProperty("GracePeriodInMin")
    private int gracePeriodInMin;

    @JsonProperty("IsEnabled")
    private boolean enabled;

    @JsonProperty("IsUnavailable")
    private boolean unavailable;


    @JsonProperty("IsProfileAware")
    private boolean profileAware;



    @JsonIgnore
    public VirtualLineStatus getStatus() {
        if (!enabled) {
            return VirtualLineStatus.DISABLED;
        }
        if (enabled) {
            if (unavailable) {
                return VirtualLineStatus.OPEN_NOT_AVAILABLE;
            }
            return VirtualLineStatus.OPEN_AVAILABLE;
        }
        return VirtualLineStatus.CLOSED;

    }




}
