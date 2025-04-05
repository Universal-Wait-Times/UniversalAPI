package me.matthewe.universal.universalapi;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api")
@RestController()
public class UniversalApiController {

    private final UniversalApiService service;

    public UniversalApiController(UniversalApiService service) {
        this.service = service;
    }

    @GetMapping("/attractions")
    public List<Attraction> getAttractions( @RequestParam(defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractions(), waitTimes);
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
            if (attraction.getQueues().size()==1) {
            }
        }
        return returnList;
    }

    @GetMapping("/orlandotest")
    public List<Attraction> getAttractionsJapanTest() {
        List<Attraction> attractions = service.fetchAttractionsByResortPark(ResortRegion.UOR, UniversalPark.IOA);

        List<Attraction> returnList = new ArrayList<Attraction>();

        for (Attraction attraction : attractions) {
            if (attraction.getQueues().size()>1){
                returnList.add(attraction);
            }
        }
        return returnList;
    }

    @GetMapping("/{resort}/attractions")
    public List<Attraction> getAttractions(@PathVariable String resort, @RequestParam(defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractionsByResort(ResortRegion.valueOf(resort.toUpperCase())), waitTimes);
    }


    @GetMapping("/{resort}/{park}/attractions")
    public List<Attraction> getAttractions(@PathVariable String resort, @PathVariable String park, @RequestParam(defaultValue = "-1") int waitTimes) {
        return applyFilters(service.fetchAttractionsByResortPark(ResortRegion.valueOf(resort.toUpperCase()), UniversalPark.getByPark(park)), waitTimes);
    }



}