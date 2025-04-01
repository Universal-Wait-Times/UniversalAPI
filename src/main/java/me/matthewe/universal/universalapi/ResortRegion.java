package me.matthewe.universal.universalapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

public enum ResortRegion {
    UOR("Universal Orlando Resort"),
    USJ("Universal Studios Japan"),
    USH("Universal Studios Hollywood");


    @Getter private String parkName;


    ResortRegion(String parkName) {
        this.parkName = parkName;
    }

    public String getCode() {
        return this.toString().toLowerCase();

    }

}
