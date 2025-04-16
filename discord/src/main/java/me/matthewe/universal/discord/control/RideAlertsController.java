package me.matthewe.universal.discord.control;

import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import me.matthewe.universal.discord.utils.DiscordWebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @PostMapping
    public String post(@RequestBody Map<String, Object> body ) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey == null) {
            return "Unable to post due to API_KEY not being setup.";
        }
        AttractionWebhookRequest request = AttractionWebhookRequest.valueOf(body);
        if (!apiKey.equals(request.getKey())) {
            return "API Key is invalid";
        }
        System.out.println(request.getKey() +" : " + request.getAttraction().getWaitTimeAttractionId());
        webhookService.sendAttractionStatusUpdate(request.getOldAttraction(), request.getAttraction());
        return "POST ATTRACTION";
    }

}
