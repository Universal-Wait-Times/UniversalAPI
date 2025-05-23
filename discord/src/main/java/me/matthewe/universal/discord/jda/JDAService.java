package me.matthewe.universal.discord.jda;


import lombok.extern.java.Log;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@Log
public class JDAService {

    public JDA jda;

    @PostConstruct
    public void start() throws Exception {
        this.jda = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN")).build();
        jda.awaitReady(); // wait for JDA to be fully loaded
        jda.getPresence().setActivity(Activity.watching("Wait Times at Epic Universe"));
        log.info("JDA started");
    }

    public JDA getJda() {
        return jda;
    }
}
