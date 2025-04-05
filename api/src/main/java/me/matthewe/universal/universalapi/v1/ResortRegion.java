package me.matthewe.universal.universalapi.v1;

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

    public String getCity() {
        return city;
    }

    public String getCode() {
        return this.toString().toLowerCase();

    }

}
