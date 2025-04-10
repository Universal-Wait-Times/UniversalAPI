package me.matthewe.universal.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UniversalImageSource {

    DEFAULT("https://i.imgur.com/e2coRke.png"),
    EPIC_UNIVERSE("https://i.imgur.com/Izg8jgR.png");
    private String source;

}
