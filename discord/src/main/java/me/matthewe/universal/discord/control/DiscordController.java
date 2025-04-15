package me.matthewe.universal.discord.control;


import me.matthewe.universal.discord.jda.JDAService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/discord")
@RestController()
public class DiscordController {

    private JDAService jdaService;

    public DiscordController(JDAService jdaService) {
        this.jdaService = jdaService;
    }

    @PostMapping
    public void post() {

    }
}
