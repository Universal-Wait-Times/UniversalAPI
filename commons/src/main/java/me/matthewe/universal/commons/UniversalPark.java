package me.matthewe.universal.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;


@AllArgsConstructor
@Getter
public enum UniversalPark {
    UEU("Universal Epic Universe", new String[]{"eu"}, UniversalImageSource.EPIC_UNIVERSE.getSource(),
            new Coords(28.439, -81.446), "Universals Epic Universe", Color.YELLOW.darker().darker(), 334),
    USF("Universal Studios Florida", new String[0], UniversalImageSource.STUDIOS_ORLANDO.getSource(),
            new Coords(28.477, -81.4681), "Universal Studios Florida", new Color(0x0368D9), 65),
    IOA("Islands of Adventure", new String[0], UniversalImageSource.ISLANDS_OF_ADVENTURE.getSource(),
            new Coords(28.471400, -81.47134667), "Universal Islands of Adventure", new Color(0x0368D9), 64),
    UPPER_LOT("Upper Lot", new String[0], UniversalImageSource.DEFAULT.getSource(),
            new Coords(34.143000, -118.36), "Universal Studios", null, 66),
    LOWER_LOT("Lower Lot", new String[0], UniversalImageSource.DEFAULT.getSource(),
            new Coords(34.14300000, -118.36), "Universal Studios", null, 66),
    USJ("Universal Studios Japan", new String[0], UniversalImageSource.DEFAULT.getSource(),
            new Coords(34.6654, 135.4323), null, null, 284);

    private String parkName;
    private String[] allies;
    private String logoSource;
    private Coords coords;
    private String displayName;
    private Color color;
    private int queueTimeId;

    public static UniversalPark getByPark(String park) {

        for (UniversalPark value : values()) {
            if (value.toString().equalsIgnoreCase(park)) {
                return value;
            }
            if (value.allies != null) {

                for (String ally : value.allies) {

                    if (ally.equalsIgnoreCase(park)) {
                        return value;
                    }
                }
            }
        }
        return null;
    }


    @Data
    @AllArgsConstructor
    private static class Coords {
        private double latitude;
        private double longitude;
    }

    public double getLatitude() {
        if (coords == null) return 0;
        return coords.getLatitude();

    }

    public double getLongitude() {
        if (coords == null) return 0;
        return coords.getLongitude();
    }
}
