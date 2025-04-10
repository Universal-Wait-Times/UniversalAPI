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


    @GetMapping
    public WeatherData getWeatherData(UniversalPark park) {
        double latitude = park.getLatitude();
        double longitude = park.getLongitude();

        if (latitude==0&&longitude==0)return  new WeatherData(0, 0);

        String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,wind_speed_10m", latitude, longitude);
        OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);

        if (response != null && response.getCurrent() != null) {
            double tempCelsius = response.getCurrent().getTemperature();
            double tempFahrenheit = (tempCelsius * 9 / 5) + 32;
            return new WeatherData(
                    response.getCurrent().getWindSpeed() * 0.621371,
                    tempFahrenheit
            );
        }

        return new WeatherData(0, 0); // Default or error case
    }
    @Data
    static class OpenMeteoResponse {
        private CurrentWeather current;

        @Data
        static class CurrentWeather {
            @JsonProperty("temperature_2m")
            private double temperature;
            @JsonProperty("wind_speed_10m")
            private double windSpeed;
        }
    }
}
