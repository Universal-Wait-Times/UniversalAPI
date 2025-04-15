package me.matthewe.universal.discord.weather;

import me.matthewe.universal.commons.UniversalPark;
import me.matthewe.universal.commons.weather.WeatherData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient webClient;

    @Autowired
    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://themeparks.matthewe.me").build();
    }

    public WeatherData getWeather(UniversalPark park) {
        if (park == null) return null;

        String parkName = park.toString();

        try {
            Mono<WeatherData> mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/weather")
                            .queryParam("parkName", parkName)
                            .build())
                    .retrieve()
                    .bodyToMono(WeatherData.class);

            return mono.block(); // Blocking to return WeatherData synchronously
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
