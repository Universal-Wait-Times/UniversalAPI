package me.matthewe.universal.universalapi.v1;

public enum UniversalPark {

    /*Orlando Parks*/
    UEU("Universal Epic Universe", "eu"),
    USF("Universal Studios Florida"),
    IOA("Islands of Adventure"),

    /*Hollywood parks*/
    UPPER_LOT("Upper Lot"),
    LOWER_LOT("Lower Lot"),

    /* Japan Parks */
    USJ("Universal Studios Japan");



    private String parkName;
    private String[] allies;

    UniversalPark(String parkName, String... allies) {
        this.parkName = parkName;
        this.allies = allies;
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
