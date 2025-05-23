package me.matthewe.universal.universalapi.v1.attractionservice;

import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.universalapi.v1.redis.RedisPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/v1/attractions")
@RestController()
public class AttractionsController {

    private final UniversalApiService service;
    private final RedisPublisher publisher;

    public AttractionsController(UniversalApiService service, RedisPublisher publisher) {
        this.service = service;
        this.publisher = publisher;
    }

    @GetMapping()
    public List<Attraction> getAttractions(@RequestParam(name = "waitTimes", defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractions(), waitTimes);
    }
    @GetMapping("test")
    public String test( ) {
        return service.fetchAttractions().size()+"";
    }



    private List<Attraction> applyFilters(List<Attraction> attractions, int waitTimes) {
        if (waitTimes==-1||waitTimes==0)return attractions;
        List<Attraction> returnList =new ArrayList<>();
        for (Attraction attraction : attractions) {

            for (Attraction.Queue queue : attraction.getQueues()) {
                if (queue.getQueueType()!= Attraction.Queue.Type.STANDBY)continue;

                if (queue.getStatus()== Attraction.Queue.Status.RIDE_NOW) {
                    returnList.add(attraction);
                    continue;
                }

                if (queue.getStatus()!= Attraction.Queue.Status.OPEN) {
                    continue;
                }

                int displayWaitTime = queue.getDisplayWaitTime();
                if (displayWaitTime!=0 && displayWaitTime<=waitTimes) {
                    returnList.add(attraction);

                }
            }
        }
        return returnList;
    }

    @GetMapping("/{resort}")
    public List<Attraction> getAttractions(@PathVariable String resort, @RequestParam(name = "waitTimes", defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractionsByResort(ResortRegion.valueOf(resort.toUpperCase())), waitTimes);
    }


    @GetMapping("/{resort}/{park}")
    public List<Attraction> getAttractions(@PathVariable String resort, @PathVariable String park, @RequestParam(name = "waitTimes", defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractionsByResortPark(ResortRegion.valueOf(resort.toUpperCase()), UniversalPark.getByPark(park)), waitTimes);
    }

}