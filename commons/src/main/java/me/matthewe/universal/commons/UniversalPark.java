package me.matthewe.universal.commons;

public enum UniversalPark {

    /*Orlando Parks*/
    UEU("Universal Epic Universe", new String[]{"eu"}, UniversalImageSource.EPIC_UNIVERSE.getSource()),
    USF("Universal Studios Florida", new String[0],UniversalImageSource.STUDIOS_ORLANDO.getSource()),
    IOA("Islands of Adventure", new String[0],UniversalImageSource.ISLANDS_OF_ADVENTURE.getSource()),

    /*Hollywood parks*/
    UPPER_LOT("Upper Lot", new String[0],UniversalImageSource.DEFAULT.getSource()),
    LOWER_LOT("Lower Lot", new String[0],UniversalImageSource.DEFAULT.getSource()),

    /* Japan Parks */
    USJ("Universal Studios Japan", new String[0],UniversalImageSource.DEFAULT.getSource());


    public String getParkName() {
        return parkName;
    }

    private String parkName;
    private String[] allies;
    private String logoSource;

    UniversalPark(String parkName, String[] allies, String logoSource) {
        this.parkName = parkName;
        this.allies = allies;
        this.logoSource = logoSource;

    }

    public String getLogoSource() {
        return logoSource;
    }

    public static UniversalPark getByPark(String park) {

        for (UniversalPark value : values()) {
            if (value.toString().equalsIgnoreCase(park)){
                return value;
            }
            if (value.allies != null) {

                for (String ally : value.allies) {

                    if (ally.equalsIgnoreCase(park)){
                        return value;
                    }
                }
            }
        }
        return null;
    }

    public String[] getAllies() {
        return allies;
    }
}
