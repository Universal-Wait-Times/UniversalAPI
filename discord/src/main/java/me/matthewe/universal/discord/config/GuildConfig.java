package me.matthewe.universal.discord.config;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GuildConfig {

    @JsonProperty("guildId")
    private long guildId;
    @JsonProperty("parkClockInChannelId")
    private long parkClockInChannelId;

    @JsonProperty("roleId")
    private long roleId;
}
