package me.matthewe.universal.discord.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@AllArgsConstructor
@Data
@Document("settings")
public class Setting {
    @Id
    private String key;

    private Object value;

}
