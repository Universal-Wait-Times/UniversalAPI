package me.matthewe.universal.commons;

import lombok.Getter;

public enum ResortRegion {
    UOR("Universal Orlando Resort", "Orlando"),
    USJ("Universal Studios Japan", "Osaka"),
    USH("Universal Studios Hollywood", "Hollywood");


    @Getter private String parkName;
    @Getter private String city;


    ResortRegion(String parkName, String city) {
        this.parkName = parkName;
        this.city = city;

    }

    public static ResortRegion getByName(String resort) {
        for (ResortRegion value : ResortRegion.values()) {
            if (value.name().equalsIgnoreCase(resort)) {
                return value;
            }
        }
        return null;
    }

    public String getCity() {
        return city;
    }

    public String getCode() {
        return this.toString().toLowerCase();

    }

}
