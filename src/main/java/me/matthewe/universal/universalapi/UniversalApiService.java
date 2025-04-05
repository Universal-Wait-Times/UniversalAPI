package me.matthewe.universal.universalapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
@EnableScheduling
@Service
public class UniversalApiService {

    private static final String URL = "https://assets.universalparks.com/%s/wait-time/wait-time-attraction-list.json";
    private static final Logger log = LogManager.getLogger(UniversalApiService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private final AtomicReference<ResortData> cache = new AtomicReference<>(new ResortData());

    private static abstract class AttractionData {
        public abstract List<Attraction> getAttractions();

    }

    public static class ResortData extends AttractionData{
        Map<ResortRegion, Resort > map =new HashMap<>();

        @Override
        public List<Attraction> getAttractions() {

            List<Attraction> attractions =new ArrayList<>();

            for (Resort value : map.values()) {
                attractions.addAll(value.getAttractions());
            }
            return attractions;

        }

        public List<Attraction> getByResortRegionByPark(ResortRegion resortRegion, UniversalPark universalPark) {
            if (map.containsKey(resortRegion)) {
                Resort resort = map.get(resortRegion);
                if (resort.map.containsKey(universalPark)) {
                    return resort.map.get(universalPark).getAttractions();

                }

            }
            return new ArrayList<>();

        }
        public List<Attraction> getByResortRegion(ResortRegion resortRegion) {
            if (map.containsKey(resortRegion)) {
                return map.get(resortRegion).getAttractions();

            }
            return new ArrayList<>();

        }
    }
    public static class Resort  extends AttractionData{
        @Override
        public List<Attraction> getAttractions() {
            List<Attraction>  attractions = new ArrayList<>();
            for (UniversalParkData value : map.values()) {
                attractions.addAll(value.getAttractions());
            }
            return attractions;
        }

        private ResortRegion resort;
        private Map<UniversalPark, UniversalParkData>map;

        public static class  UniversalParkData extends AttractionData{
            private  UniversalPark  universalPark;
            private Map<String, Attraction> shows;
            private Map<String, Attraction> rides;

            public UniversalParkData(UniversalPark universalPark) {
                this.universalPark = universalPark;
                this.shows = new HashMap<>();
                this.rides = new HashMap<>();
            }

            public boolean update(Attraction attraction, boolean show) {
                if (show) {
                    if (shows.containsKey(attraction.getWaitTimeAttractionId())) {

                        shows.put(attraction.getWaitTimeAttractionId(), attraction);
                        return true;
                    }
                    shows.put(attraction.getWaitTimeAttractionId(), attraction);
                    return false;

                } else {
                    if (rides.containsKey(attraction.getWaitTimeAttractionId())) {

                        rides.put(attraction.getWaitTimeAttractionId(), attraction);
                        return true;
                    }
                    rides.put(attraction.getWaitTimeAttractionId(), attraction);
                    return false;
                }
            }
            @Override
            public List<Attraction> getAttractions() {
                List<Attraction>  attractions = new ArrayList<>();
                attractions.addAll(shows.values());
                attractions.addAll(rides.values());
                return attractions;
            }
        }
    }
    public UniversalApiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Attraction> fetchAttractions() {
        return cache.get().getAttractions();

    }
    public List<Attraction> fetchAttractionsByResort(ResortRegion resortRegion) {
        return cache.get().getByResortRegion(resortRegion);
    }

   public List<Attraction> fetchAttractionsByResortPark(ResortRegion resortRegion, UniversalPark universalPark) {
        return cache.get().getByResortRegionByPark(resortRegion, universalPark);
    }

    @PostConstruct
    public void initialLoad() {
        refreshCache();
    }

    @Scheduled(fixedRate = 10_000)
    public void refreshCache() {
        try {
            ResortData resortData;
            if (cache.get()!=null){
                resortData=cache.get();

            } else {
                resortData = new ResortData();
                resortData.map = new HashMap<>();
            }

            for (ResortRegion value : ResortRegion.values()) {

                String json = restTemplate.getForObject(String.format(URL, value.toString().toLowerCase()), String.class);
                List<Attraction> attractions = objectMapper.readValue(json, new TypeReference<>() {});


//                System.err.println(json);




                for (Attraction attraction : attractions) {


                    ResortRegion resortAreaCode = attraction.getResortAreaCode();

                    Resort resort = resortData.map.getOrDefault(resortAreaCode, new Resort());
                    if (resort.resort==null){

                        resort.resort = resortAreaCode;
                    }

                    resortData.map.put(resortAreaCode,resort);


                    if (resort.map==null)resort.map = new HashMap<>();



                    String waitTimeAttractionId = attraction.getWaitTimeAttractionId();

                    String[] split = waitTimeAttractionId.split("\\.");




                    String park = split[1];


                    UniversalPark universalPark = UniversalPark.getByPark(park);

                    if (waitTimeAttractionId.equals("ush.rides.secret_life_of_pets")) { //Dealing with strange edge case
                        universalPark = UniversalPark.USJ;

                    }
                    if (universalPark==null){
                        log.warn(waitTimeAttractionId);
                        log.error("Universal park " + park + " doesn't exist?");
                        continue;
                    }
                    Resort.UniversalParkData parkData = resort.map.getOrDefault(universalPark, new Resort.UniversalParkData(universalPark));


                    String type = split[2];
                    if (type.equalsIgnoreCase("rides")) {

                        parkData.update(attraction, false);
                    } else {
                        parkData.update(attraction, true);

                    }

                    resort.map.put(universalPark, parkData);


                }
            }

            cache.set(resortData);
        } catch (Exception e) {
            log.error("Failed to refresh attraction cache: " + e.getMessage());
        }
    }
}
