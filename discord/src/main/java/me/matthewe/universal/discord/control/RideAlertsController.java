package me.matthewe.universal.discord.control;

import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import me.matthewe.universal.discord.utils.DiscordWebhookService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String post(Attraction oldAttraction, Attraction attraction, String key) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey==null) {
            return "Unable to post due to API_KEY not being setup.";
        }
        if (!key.equals(apiKey)) {
            return "API Key is invalid";
        }
        webhookService.sendAttractionStatusUpdate(oldAttraction, attraction);
        return "POST ATTRACTION";
    }
}
