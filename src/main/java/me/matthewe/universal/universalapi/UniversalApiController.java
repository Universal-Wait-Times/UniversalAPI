package me.matthewe.universal.universalapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<Attraction> getAttractions() {
        return service.fetchAttractions();
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

    @GetMapping("/{resort}/{park}/attractions")
    public List<Attraction> getAttractions(@PathVariable String resort, @PathVariable String park) {
        return service.fetchAttractionsByResortPark(ResortRegion.valueOf(resort.toUpperCase()), UniversalPark.getByPark(park));
    }



}