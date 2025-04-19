package me.matthewe.universal.universalapi.v1.hours;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.universalapi.v1.venue.Venue;
import me.matthewe.universal.universalapi.v1.venue.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/v1/parks/hours")
@RestController()

public class ParkHoursController {

    private VenueService venueService;
    private ObjectMapper objectMapper;
    public ParkHoursController(VenueService venueService) {
        this.venueService = venueService;
        objectMapper =new ObjectMapper();
    }

    @GetMapping("/{resort}/{venueName}")
    public String getHours(@RequestBody  ResortRegion resortRegion, String venueName) throws Exception {
        if (resortRegion==ResortRegion.USJ) {
            throw new Exception("Cannot check hours at Universal Japan.");
        }
        Map<String, Venue> venueMap = venueService.get(resortRegion);
        if (venueMap==null){
            throw new Exception("Venue map null");
        }
        if (!venueMap.containsKey(venueName)){
            throw new Exception("Venue " + venueName + " not found");
        }
        Venue venue = venueMap.get(venueName);

        return objectMapper.writeValueAsString(venue.getHours());

    }
}
