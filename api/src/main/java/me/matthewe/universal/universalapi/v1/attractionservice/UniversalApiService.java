package me.matthewe.universal.universalapi.v1.attractionservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import me.matthewe.universal.commons.ResortRegion;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.universalapi.gson.GsonUtils;
import me.matthewe.universal.universalapi.utils.DiscordWebhookUtil;
import me.matthewe.universal.universalapi.v1.redis.RedisPublisher;
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
    private final RedisPublisher redisPublisher;


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

            private  void onUpdate(Attraction oldAttraction, Attraction newAttraction, boolean show, boolean initial, RedisPublisher redisPublisher) {
                boolean updatedStatus = false;

                if (oldAttraction == null) {
                    updatedStatus = true;

                } else {

                    if (oldAttraction.getTempId().equals(newAttraction.getTempId())) {
                        return;
                    }

                    Attraction.Queue.Status oldStatus = oldAttraction.getQueues().get(0).getStatus();
                    Attraction.Queue.Status newStatus = newAttraction.getQueues().get(0).getStatus();

                    if (oldStatus != newStatus) {
                        updatedStatus = true;
                    }
                }
                if (updatedStatus) {
                    try {
                        // Create a JSON payload with both old and new attraction objects.

                        JsonObject jsonObject = new JsonObject();


                        Gson GSON = GsonUtils.GSON_SHORT;
                        if (oldAttraction != null) {
                            jsonObject.add("oldAttraction", GSON.fromJson(GSON.toJson(oldAttraction, Attraction.class), JsonObject.class));

                        }
                        if (newAttraction != null) {

                            jsonObject.add("newAttraction", GSON.fromJson(GSON.toJson(newAttraction, Attraction.class), JsonObject.class));
                        }

                        DiscordWebhookUtil.sendAttractionStatusUpdate(oldAttraction, newAttraction);
//                        redisPublisher.publish("ride-status-update", GSON.toJson(jsonObject));
                    } catch (Exception e) {
                        // Log or handle the serialization error appropriately.
                        System.err.println("Failed to publish JSON payload: " + e.getMessage());
                    }
                }
            }


            public boolean update(Attraction attraction, boolean show, RedisPublisher redisPublisher) {
                if (show) {
                    if (shows.containsKey(attraction.getWaitTimeAttractionId())) {
                        final Attraction oldShow = shows.get(attraction.getWaitTimeAttractionId());
                        shows.put(attraction.getWaitTimeAttractionId(), attraction);
                        onUpdate(oldShow, attraction,show, false,redisPublisher);
                        return true;
                    }
                    shows.put(attraction.getWaitTimeAttractionId(), attraction);
                    onUpdate(null, attraction,show, true,redisPublisher);
                    return false;

                } else {
                    if (rides.containsKey(attraction.getWaitTimeAttractionId())) {
                        final Attraction oldRide = rides.get(attraction.getWaitTimeAttractionId());

                        rides.put(attraction.getWaitTimeAttractionId(), attraction);
                        onUpdate(oldRide, attraction,show, false,redisPublisher);
                        return true;
                    }
                    rides.put(attraction.getWaitTimeAttractionId(), attraction);
                    onUpdate(null, attraction,show, true,redisPublisher);
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

    public UniversalApiService(RestTemplate restTemplate, ObjectMapper objectMapper, RedisPublisher redisPublisher) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
        this.redisPublisher = redisPublisher;
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

    private static final int MIN_DELAY = 15000; // 15 seconds
    private static final int MAX_DELAY = 30000; // 30 seconds

    private long nextExecutionTime = 0;
    private Random random = new Random();


    //Slowed down pull rate to remain compliant with Universal TOS.
    @Scheduled(fixedRate = 1000)
    public void refreshCache() {

        try {

            long currentTime = System.currentTimeMillis();

            if (currentTime >= nextExecutionTime) {

                int delay = MIN_DELAY + random.nextInt(MAX_DELAY - MIN_DELAY);
                nextExecutionTime = currentTime + delay;
            }

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
                    attraction.setTempId(UUID.randomUUID());

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

                    attraction.setPark(universalPark);
                    if (waitTimeAttractionId.equals("ush.rides.secret_life_of_pets")) { //Dealing with strange edge case.
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

                        parkData.update(attraction, false,redisPublisher);
                    } else {
                        parkData.update(attraction, true,redisPublisher);

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
