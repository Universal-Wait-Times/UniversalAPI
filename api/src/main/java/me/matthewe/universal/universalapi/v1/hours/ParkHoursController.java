package me.matthewe.universal.universalapi.v1.hours;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.universalapi.v1.venue.Venue;
import me.matthewe.universal.universalapi.v1.venue.VenueService;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequestMapping("/api/v1/parks/hours")
@RestController()

public class ParkHoursController {

    private VenueService venueService;
    private final SimpleDateFormat formatter =new SimpleDateFormat("MM-dd-yyyy");
    public ParkHoursController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/{resort}/{venueName}/{date}")
    public Map<String, Venue.Hours> getHoursByDate(
            @PathVariable String resort,
            @PathVariable String venueName,
            @PathVariable String date) throws Exception {

        ResortRegion resortRegion = ResortRegion.getByName(resort);
        if (resortRegion == null) {
            throw new Exception("Null resort region " + resort);
        }

        if (resortRegion == ResortRegion.USJ) {
            throw new Exception("Cannot check hours at Universal Japan.");
        }

        UniversalPark byPark = UniversalPark.getByPark(venueName);
        if (byPark != null) {
            venueName = byPark.getDisplayName();
        }

        Map<String, Venue> venueMap = venueService.get(resortRegion);
        if (venueMap == null) {
            throw new Exception("Venue map null");
        }

        if (!venueMap.containsKey(venueName)) {
            throw new Exception("Venue " + venueName + " not found");
        }

        Venue venue = venueMap.get(venueName);
        Map<String, Venue.Hours> hours = new LinkedHashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

        for (Venue.Hours h : venue.getHours()) {
            String formatted = sdf.format(h.getDate());
            if (formatted.equals(date)) {
                hours.put(date, h);
                break;
            }
        }

        if (hours.isEmpty()) {
            throw new Exception("No hours found for date " + date);
        }

        return hours;
    }

    @GetMapping("/{resort}/{venueName}")
    public Map<String, Venue.Hours> getHours(@PathVariable String resort, @PathVariable String venueName) throws Exception {
        ResortRegion resortRegion = ResortRegion.getByName(resort);
        if (resortRegion==null){
            throw new Exception("Null resort region " + resort);
        }
        if (resortRegion==ResortRegion.USJ) {
            throw new Exception("Cannot check hours at Universal Japan.");
        }
        UniversalPark byPark = UniversalPark.getByPark(venueName);
        if (byPark!=null) {
            venueName = byPark.getDisplayName();
        }
        Map<String, Venue> venueMap = venueService.get(resortRegion);
        if (venueMap==null){
            throw new Exception("Venue map null");
        }
        if (!venueMap.containsKey(venueName)){
            throw new Exception("Venue " + venueName + " not found");
        }
        Venue venue = venueMap.get(venueName);
        Map<String, Venue.Hours> hours = new LinkedHashMap<>(); // preserve order
        List<Venue.Hours> hoursList = new ArrayList<>(venue.getHours());

// Sort from closest (earliest) to latest
        hoursList.sort(Comparator.comparing(h -> h.getDate())); // assuming getDate() returns OffsetDateTime or similar


        for (Venue.Hours hour : hoursList) {
            hours.put(formatter.format(hour.getDate()), hour);
        }
        return hours;


    }
}
