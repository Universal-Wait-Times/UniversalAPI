package me.matthewe.universal.discord.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.virtualline.VirtualLine;

import java.util.Map;

@Data
public class VirtualLineWebhookRequest {
    private VirtualLine oldVirtualLine;
    private VirtualLine newVirtualLine;
    private String key;

    public static VirtualLineWebhookRequest valueOf(Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        VirtualLineWebhookRequest req = new VirtualLineWebhookRequest();

        req.setKey((String) map.get("key"));
        req.setNewVirtualLine(mapper.convertValue(map.get("newLine"), VirtualLine.class));

        if (map.containsKey("oldLine") && map.get("oldLine") != null) {
            req.setOldVirtualLine(mapper.convertValue(map.get("oldLine"), VirtualLine.class));
        }

        return req;
    }
}