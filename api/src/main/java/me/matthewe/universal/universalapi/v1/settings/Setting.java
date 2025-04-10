package me.matthewe.universal.universalapi.v1.settings;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("settings")
public class Setting {
    @Id
    private String key;

    private Object value;

}
