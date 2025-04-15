package me.matthewe.universal.discord.jda;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

@Log
@Component
public class JDAComponent {



    @PostConstruct
    public void init() {
        log.info("JDAComponent initialized");
    }
}
