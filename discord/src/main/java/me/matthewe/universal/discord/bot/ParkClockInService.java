package me.matthewe.universal.discord.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.discord.jda.JDAService;
import me.matthewe.universal.discord.settings.SettingService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@Log
public class ParkClockInService extends ListenerAdapter {
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
        MessageHistory complete = textChannel.getHistoryAfter(1362520266806264099L, 100).complete();
        for (Message message : complete.getRetrievedHistory()) {
            message.delete().complete();
        }

        textChannel.sendMessageEmbeds(new EmbedBuilder()
                        .setColor(UniversalPark.UEU.getColor())
                        .setDescription("Epic Universe Virtual Queue Clock-In")
                        .setTimestamp(OffsetDateTime.now())
                        .setFooter("Virtual Queue", "https://i.imgur.com/jPvBkcc.png")
                        .build())
                .setActionRow(
                        Button.primary("clock_in", "Clock In"),
                        Button.danger("cancel", "Clock Out")
                )
                .queue();


        service.jda.addEventListener(this);


    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        Member member = event.getMember(); // who clicked
        Guild guild = event.getGuild();

        if (member == null || guild == null) {
            event.reply("Error: Could not process your request.").setEphemeral(true).queue();
            return;
        }

        Role clockInRole = guild.getRoleById(1363206630321422466L); // Your role ID

        if (clockInRole == null) {
            event.reply("Error: Clock-in role not found.").setEphemeral(true).queue();
            return;
        }

        switch (buttonId) {
            case "clock_in":
                if (!member.getRoles().contains(clockInRole)) {
                    guild.addRoleToMember(member, clockInRole).queue();
                    event.reply("✅ You have clocked in and the role has been assigned.").setEphemeral(true).queue();
                } else {
                    event.reply("⚠️ You are already clocked in.").setEphemeral(true).queue();
                }
            
                break;

            case "cancel":
                if (member.getRoles().contains(clockInRole)) {
                    guild.removeRoleFromMember(member, clockInRole).queue();
                    event.reply("❌ You have clocked out and the role has been removed.").setEphemeral(true).queue();
                } else {
                    event.reply("⚠️ You weren't clocked in.").setEphemeral(true).queue();
                }


                break;

            default:
                event.reply("Unknown action.").setEphemeral(true).queue();
                break;
        }
    }
}
