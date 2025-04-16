package me.matthewe.universal.discord.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import me.matthewe.universal.commons.Attraction;

import java.util.Map;

@Data
public class AttractionWebhookRequest {
    private Attraction oldAttraction;
    private Attraction attraction;
    private String key;

    public static AttractionWebhookRequest valueOf(Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        AttractionWebhookRequest req = new AttractionWebhookRequest();

        req.setKey((String) map.get("key"));
        req.setAttraction(mapper.convertValue(map.get("attraction"), Attraction.class));

        if (map.containsKey("oldAttraction") && map.get("oldAttraction") != null) {
            req.setOldAttraction(mapper.convertValue(map.get("oldAttraction"), Attraction.class));
        }

        return req;
    }
}