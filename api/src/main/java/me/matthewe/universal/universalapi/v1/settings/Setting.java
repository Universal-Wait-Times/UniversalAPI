package me.matthewe.universal.universalapi.v1.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
