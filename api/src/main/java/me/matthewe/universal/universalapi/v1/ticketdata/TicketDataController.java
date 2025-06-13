package me.matthewe.universal.universalapi.v1.ticketdata;

import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.ticketdata.TicketData;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequestMapping("/api/v1/ticket_data")
@RestController()
public class TicketDataController {

    private TicketDataService ticketDataService;

    @Autowired
    public TicketDataController(TicketDataService ticketDataService) {
        this.ticketDataService = ticketDataService;
    }

    @GetMapping("/{resort}")
    public Map<String, TicketData> getData(
            @PathVariable String resort,
            @RequestParam String date
    ) {
        ResortRegion region = ResortRegion.getByName(resort);
        if (region == null || region == ResortRegion.USJ || region == ResortRegion.USH) {
            return Collections.emptyMap();
        }

        String norm = date.replace('/', '-').trim();
        String[] parts = norm.split("-");
        if (parts.length != 3) {
            return Collections.emptyMap();
        }

        try {
            int month = Integer.parseInt(parts[0]);
            int day = Integer.parseInt(parts[1]);
            String y = parts[2];
            int year;

            if (y.length() == 2) {
                year = 2000 + Integer.parseInt(y);
            } else if (y.length() == 4) {
                year = Integer.parseInt(y);
            } else {
                return Collections.emptyMap();
            }

            LocalDate parsed = LocalDate.of(year, month, day);
            String key = parsed.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));

            Map<String, TicketData> dataCache = ticketDataService.getDataCache();
            if (!dataCache.containsKey(key)) {
                return Collections.emptyMap();
            }
            return Collections.singletonMap(key, dataCache.get(key));
        } catch (NumberFormatException | DateTimeException e) {
            return Collections.emptyMap();
        }
    }

    @GetMapping("/{resort}/all")
    public Map<String, TicketData> getData(@PathVariable String resort) {
        ResortRegion resortRegion = ResortRegion.getByName(resort);

        Map<String, TicketData> ticketData = new HashMap<>();
        if (resortRegion == null) return ticketData;

        if (resortRegion == ResortRegion.USJ) return ticketData;
        if (resortRegion == ResortRegion.USH) return ticketData;

        Map<String, TicketData> dataCache = ticketDataService.getSortedDataCache();
        if (dataCache == null) return ticketData;

        return dataCache;
    }
}
