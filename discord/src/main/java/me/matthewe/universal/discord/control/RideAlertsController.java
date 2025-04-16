package me.matthewe.universal.discord.control;

import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import me.matthewe.universal.discord.utils.DiscordWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log
@RequestMapping("/api/v1/discord/ride_alerts")
@RestController()
public class RideAlertsController {
    private JDAService jdaService;
    private SettingService settingService;
    private DiscordWebhookService webhookService;

    public RideAlertsController(JDAService jdaService, SettingService settingService, DiscordWebhookService webhookService) {
        this.jdaService = jdaService;
        this.settingService = settingService;
        this.webhookService = webhookService;
    }
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

    @PostMapping
    public String post(@RequestBody Map<String, Object> body) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey == null) {
            return "Unable to post due to API_KEY not being setup.";
        }

        AttractionWebhookRequest request;
        try {
            request = AttractionWebhookRequest.valueOf(body);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse request: " + e.getMessage();
        }

        if (!apiKey.equals(request.getKey())) {
            return "API Key is invalid";
        }

        Attraction attraction = request.getAttraction();
        if (attraction == null) {
            return "Missing attraction data.";
        }


        if (attraction.getPark() == null) {
            log.warning("Park is null for: " + attraction.getWaitTimeAttractionId());
            // optionally infer from resort_area_code or skip
            return "Missing park";
        }


        System.out.println("Posting alert: " + attraction.getWaitTimeAttractionId());
        webhookService.sendAttractionStatusUpdate(request.getOldAttraction(), attraction);
        return "POST ATTRACTION";
    }

}
