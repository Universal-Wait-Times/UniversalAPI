package me.matthewe.universal.discord.control;

import lombok.Data;
import me.matthewe.universal.commons.Attraction;

@Data
public class AttractionWebhookRequest {
    private Attraction oldAttraction;
    private Attraction attraction;
    private String key;
}