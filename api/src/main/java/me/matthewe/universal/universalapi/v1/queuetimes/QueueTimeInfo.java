package me.matthewe.universal.universalapi.v1.queuetimes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueTimeInfo {
    private String date;
    private int crowdPercent;
    private int r;
    private int g;
    private int b;

    @JsonProperty("color")
    public String getColor() {
        return "rgb(" + r + "," + g + "," + b + ")";
    }
}
