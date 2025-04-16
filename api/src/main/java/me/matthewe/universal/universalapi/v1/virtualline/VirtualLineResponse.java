package me.matthewe.universal.universalapi.v1.virtualline;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.matthewe.universal.commons.virtualline.VirtualLine;

import java.util.List;

@Data
public class VirtualLineResponse {
    @JsonProperty("TotalCount")
    private int totalCount;

    @JsonProperty("Pages")
    private int pages;

    @JsonProperty("PreviousPage")
    private String previousPage;

    @JsonProperty("NextPage")
    private String nextPage;

    @JsonProperty("Results")
    private List<VirtualLine> results;
}
