package me.matthewe.universal.universalapi.v1.weather;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WeatherData {
    private double windSpeed;
    private double temperature;
}
