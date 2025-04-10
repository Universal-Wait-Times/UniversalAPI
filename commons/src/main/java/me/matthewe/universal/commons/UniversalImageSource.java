package me.matthewe.universal.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UniversalImageSource {

    DEFAULT("https://i.imgur.com/e2coRke.png"),
    EPIC_UNIVERSE("https://i.imgur.com/Izg8jgR.png"),
    ISLANDS_OF_ADVENTURE("https://i.imgur.com/1mg9A7s.png"),
    STUDIOS_ORLANDO("https://i.imgur.com/h3tPr7O.png")
    ;
    private String source;

}
