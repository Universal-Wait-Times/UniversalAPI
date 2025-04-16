package me.matthewe.universal.discord.control;

import lombok.Data;
import me.matthewe.universal.commons.Attraction;

import java.util.Map;

@Data
public class AttractionWebhookRequest {
    private Attraction oldAttraction;
    private Attraction attraction;
    private String key;

    public static AttractionWebhookRequest valueOf(Map<String, Object> body) {
        AttractionWebhookRequest attractionWebhookRequest = new AttractionWebhookRequest();

        attractionWebhookRequest.setKey((String) body.get("key"));
        attractionWebhookRequest.setAttraction((Attraction) body.get("attraction"));
        if (body.containsKey("oldAttraction")) {
            attractionWebhookRequest.setOldAttraction((Attraction) body.get("oldAttraction"));
        }
        return attractionWebhookRequest;

    }
}