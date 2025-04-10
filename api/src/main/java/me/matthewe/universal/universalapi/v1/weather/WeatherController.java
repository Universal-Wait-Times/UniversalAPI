package me.matthewe.universal.universalapi.v1.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import me.matthewe.universal.commons.UniversalPark;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/api/v1/weather")
@RestController()
public class WeatherController {

    //https://api.open-meteo.com/v1/forecast?latitude=28.5&longitude=-81.4&current=temperature_2m,wind_speed_10m
    private final RestTemplate restTemplate = new RestTemplate();


    public static WeatherData request(UniversalPark park) {
        if (park == null) return null;

        String parkName = park.toString(); // Assumes UniversalPark has getName()

        String url = String.format("https://themeparks.matthewe.me/api/v1/weather?parkName=%s", parkName);
        RestTemplate restTemplate = new RestTemplate();

        try {
            return restTemplate.getForObject(url, WeatherData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getWeatherEmoji(int code) {
        if (code >= 0 && code <= 1) return "☀️";          // Clear
        if (code == 2) return "⛅";                       // Partly Cloudy
        if (code == 3) return "☁️";                       // Overcast
        if (code >= 45 && code <= 48) return "🌫️";        // Fog
        if (code >= 51 && code <= 57) return "🌦️";        // Drizzle
        if (code >= 61 && code <= 67) return "🌧️";        // Rain
        if (code >= 71 && code <= 77) return "❄️";        // Snow
        if (code >= 80 && code <= 82) return "🌧️";        // Rain showers
        if (code >= 95 && code <= 99) return "⛈️";         // Thunderstorm
        return "❓"; // Unknown
    }

    @GetMapping
    public WeatherData getWeatherData(String parkName) {
        UniversalPark park = UniversalPark.getByPark(parkName);
        if (park == null) return null;

        double latitude = park.getLatitude();
        double longitude = park.getLongitude();

        if (latitude == 0 && longitude == 0) return null;

        String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,wind_speed_20m,precipitation,precipitation_probability,weather_code,wind_gusts_10m", latitude, longitude);
        OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (response != null && response.getCurrent() != null) {

            double tempCelsius = response.getCurrent().getTemperature();
            double tempFahrenheit = (tempCelsius * 9 / 5) + 32;
            return new WeatherData(
                    response.getCurrent().getWindSpeed() * 0.621371,
                    tempFahrenheit,
                    response.getCurrent().precipitation * 0.0393701,
                    response.current.precipitationProbability,
                    response.current.weatherCode,
                    response.getCurrent().getWindGusts() * 0.621371


            );
        }

        return null; // Default or error case
    }

    @Data
    static class OpenMeteoResponse {
        private CurrentWeather current;

        @Data
        static class CurrentWeather {
            @JsonProperty("temperature_2m")
            private double temperature;
            @JsonProperty("wind_speed_20m")
            private double windSpeed;


            @JsonProperty("precipitation")
            private double precipitation;

            @JsonProperty("precipitation_probability")
            private double precipitationProbability;
            @JsonProperty("weather_code")
            private int weatherCode;
            @JsonProperty("wind_gusts_10m")
            private double windGusts;
        }
    }
}