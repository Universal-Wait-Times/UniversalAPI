package me.matthewe.universal.commons.weather;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeatherData {

    private double windSpeed;
    private double temperature;
    private double precipitation;
    private double precipitationProbability;
    private int weatherCode;
    private double windGusts;

    @JsonProperty("weatherEmoji")
    private String weatherEmoji;

    public String getWeatherEmoji() {
        if (weatherEmoji != null) {
            return weatherEmoji;
        }
        weatherEmoji = buildEmote(weatherCode);
        return weatherEmoji;
    }

    private String buildEmote(int weatherCode) {
        if (weatherCode >= 0 && weatherCode <= 1) return "☀️";      // Clear
        if (weatherCode == 2) return "⛅";                // Partly Cloudy
        if (weatherCode == 3) return "☁️";                // Overcast
        if (weatherCode >= 45 && weatherCode <= 48) return "🌫️";    // Fog
        if (weatherCode >= 51 && weatherCode <= 57) return "🌦️";    // Drizzle
        if (weatherCode >= 61 && weatherCode <= 67) return "🌧️";    // Rain
        if (weatherCode >= 71 && weatherCode <= 77) return "❄️";    // Snow
        if (weatherCode >= 80 && weatherCode <= 82) return "🌧️";    // Showers
        if (weatherCode >= 95 && weatherCode <= 99) return "⛈️";     // Thunderstorm
        return "❓";
    }
}
