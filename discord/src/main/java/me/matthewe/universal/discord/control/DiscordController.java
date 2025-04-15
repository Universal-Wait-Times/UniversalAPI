package me.matthewe.universal.discord.control;


import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/discord")
@RestController()
public class DiscordController {

    private JDAService jdaService;
    private SettingService settingService;

    public DiscordController(JDAService jdaService, SettingService settingService) {
        this.jdaService = jdaService;
        this.settingService = settingService;
    }

    @PostMapping
    public void post() {

        settingService.setSetting("test", "test");
    }
}
