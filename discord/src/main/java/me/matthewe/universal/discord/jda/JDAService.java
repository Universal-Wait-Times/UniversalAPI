package me.matthewe.universal.discord.jda;


import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Log
public class JDAService {

    private JDA jda;

    @PostConstruct
    public void start() throws Exception {
        this.jda = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN")).build();
        jda.awaitReady(); // wait for JDA to be fully loaded
        log.info("JDA started");
    }

    public JDA getJda() {
        return jda;
    }
}
