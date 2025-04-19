package me.matthewe.universal.discord.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log
public class ParkClockInService {
    private JDAService service;
    private SettingService settingService;

    @Autowired
    public ParkClockInService(JDAService service, SettingService settingService) {
        this.service = service;
        this.settingService = settingService;
    }

    @PostConstruct
    public void start() throws Exception {
        settingService.setSetting("guildID", 1356312025223135292L);

        Guild guild = service.jda.getGuildById((long) (settingService.getSetting("guildID")));
        if (guild==null){
            log.info("ERROR (1)");

            return;
        }
        TextChannel textChannel = guild.getTextChannelById(1362519999662653501L);
        if (textChannel==null) {
            log.info("ERROR (2)");
            return;
        }

        textChannel.getHistoryAfter(1362520266806264099L, 100).queue(messageHistory -> {
            for (Message message : messageHistory.getRetrievedHistory()) {
                message.delete().queue();
            }
        });


    }
}
