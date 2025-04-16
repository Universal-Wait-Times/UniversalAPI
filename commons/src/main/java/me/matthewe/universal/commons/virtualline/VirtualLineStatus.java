package me.matthewe.universal.commons.virtualline;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
@Getter
public enum VirtualLineStatus {
    DISABLED(Color.gray, "Disabled"),
    CLOSED(Color.red, "Closed"),

    OPEN_NOT_AVAILABLE(Color.yellow.darker(), "No Longer Available"),
    OPEN_AVAILABLE(Color.green.darker(), "Now Available");

    private Color color;
    private String name;
}
