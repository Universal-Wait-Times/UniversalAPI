package me.matthewe.universal.discord.bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.discord.config.GuildConfig;
import me.matthewe.universal.discord.config.GuildConfigService;
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
import java.util.concurrent.TimeUnit;

@Service
@Log
public class ParkClockInService extends ListenerAdapter {
    private JDAService service;
    private SettingService settingService;
    private GuildConfigService configService;

    @Autowired
    public ParkClockInService(JDAService service, SettingService settingService, GuildConfigService configService) {
        this.service = service;
        this.settingService = settingService;
        this.configService = configService;
    }

    @PostConstruct
    public void start() throws Exception {
        for (GuildConfig allConfig : configService.getAllConfigs()) {

            Guild guild = service.jda.getGuildById(allConfig.getGuildId());
            if (guild==null){
                log.info("ERROR (1)");

                continue;
            }
            TextChannel textChannel = guild.getTextChannelById(allConfig.getParkClockInChannelId());
            if (textChannel==null) {
                log.info("ERROR (2)");
                continue;
            }

            MessageHistory complete = textChannel.getHistoryFromBeginning(100).complete();
            for (Message message : complete.getRetrievedHistory()) {
                message.delete().complete();
            }
            textChannel.sendMessageEmbeds(new EmbedBuilder()
                            .setColor(UniversalPark.UEU.getColor())
                            .setDescription("Use this channel to clock into specific parks. A bot message will appear with buttons — click the button for the park you're visiting.\n" +
                                    "\n" +
                                    "Clocking in grants access to Battle of the Ministry virtual queue alerts only. You'll be notified when the ride becomes available or queue times change significantly.")
                            .setTimestamp(OffsetDateTime.now())
                            .setFooter("Virtual Queue", "https://i.imgur.com/jPvBkcc.png")
                            .build())
                    .complete();

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
        }



        service.jda.addEventListener(this);


    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        Member member = event.getMember(); // who clicked
        Guild guild = event.getGuild();

        if (member == null || guild == null) {
            event.reply("Error: Could not process your request.") .setEphemeral(true)
                    .queue(interactionHook ->
                            interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                    );
            return;
        }

        GuildConfig config = configService.getConfig(guild.getIdLong());
        if (config==null)return;

        Role clockInRole = guild.getRoleById(config.getRoleId()); // Your role ID

        if (clockInRole == null) {
            event.reply("Error: Clock-in role not found.") .setEphemeral(true)
                    .queue(interactionHook ->
                            interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                    );
            return;
        }

        switch (buttonId) {
            case "clock_in":
                if (!member.getRoles().contains(clockInRole)) {
                    guild.addRoleToMember(member, clockInRole).queue();
                    event.reply("✅ You have clocked in and the role has been assigned.") .setEphemeral(true)
                            .queue(interactionHook ->
                                    interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                            );
                } else {
                    event.reply("⚠️ You are already clocked in.") .setEphemeral(true)
                            .queue(interactionHook ->
                                    interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                            );
                }

                break;

            case "cancel":
                if (member.getRoles().contains(clockInRole)) {
                    guild.removeRoleFromMember(member, clockInRole).queue();
                    event.reply("❌ You have clocked out and the role has been removed.") .setEphemeral(true)
                            .queue(interactionHook ->
                                    interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                            );
                } else {
                    event.reply("⚠️ You weren't clocked in.").setEphemeral(true)
                            .queue(interactionHook ->
                                    interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                            );
                }


                break;

            default:
                event.reply("Unknown action.") .setEphemeral(true)
                        .queue(interactionHook ->
                                interactionHook.deleteOriginal().queueAfter(3, TimeUnit.SECONDS)
                        );
                break;
        }
    }
}
