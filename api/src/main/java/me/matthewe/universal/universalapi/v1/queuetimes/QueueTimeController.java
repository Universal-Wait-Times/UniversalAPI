package me.matthewe.universal.universalapi.v1.queuetimes;

import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/queue_times")
@RestController()
public class QueueTimeController {
    private QueueTimesService queueTimesService;

    @Autowired
    public QueueTimeController(QueueTimesService queueTimesService) {
        this.queueTimesService = queueTimesService;
    }
    @GetMapping("/{resort}/{park}/crowd_level")
    public QueueTimeInfo getCrowdLevels(@PathVariable String resort,
                                        @PathVariable String park,
                                        @RequestParam String date) {

        String key = park.toUpperCase() + "-" + date; // match how you stored it
        Cache cache =getCrowdLevels(resort, park);
        if (cache == null) return null;

        Cache.ValueWrapper valueWrapper = cache.get(key);
        if (valueWrapper == null) return null;

        return (QueueTimeInfo) valueWrapper.get(); // cast and return
    }


    @GetMapping("/{resort}/{park}/crowd_level")
    public Cache getCrowdLevels(@PathVariable String resort, @PathVariable String park) {
        ResortRegion resortRegion = ResortRegion.getByName(resort);

        if (resortRegion == null) return null;
        UniversalPark universalPark = UniversalPark.getByPark(park);

        if (universalPark == null) return null;

        Cache cache = queueTimesService.getCacheManager().getCache("queueTimes-" + universalPark.name());
        return cache;

    }
}

