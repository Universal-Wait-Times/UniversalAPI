package me.matthewe.universal.discord.control;

import lombok.extern.java.Log;
import me.matthewe.universal.commons.Attraction;
import me.matthewe.universal.commons.virtualline.VirtualLine;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import me.matthewe.universal.discord.utils.DiscordWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Log
@RequestMapping("/api/v1/discord/line_alerts")
@RestController()
public class VirtualLineAlertsController {
    private JDAService jdaService;
    private SettingService settingService;
    private DiscordWebhookService webhookService;

    public VirtualLineAlertsController(JDAService jdaService, SettingService settingService, DiscordWebhookService webhookService) {
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

        VirtualLineWebhookRequest request;
        try {
            request = VirtualLineWebhookRequest.valueOf(body);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to parse request: " + e.getMessage();
        }

        if (!apiKey.equals(request.getKey())) {
            return "API Key is invalid";
        }

        VirtualLine virtualLine = request.getNewVirtualLine();
        if (virtualLine == null) {
            return "Missing virtualLine data.";
        }



        webhookService.sendVirtualLineStatusUpdate(request.getOldVirtualLine(), virtualLine);
        return "POST ATTRACTION";
    }

}
