package me.matthewe.universal.universalapi.v1.hours;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import me.matthewe.universal.universalapi.openai.OpenAIService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Log
@Service
public class EpicHoursService {

    private Map<Date, String> hoursMap;
    private OpenAIService openAIService;
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter;

    public EpicHoursService(OpenAIService openAIService) {
        this.openAIService = openAIService;
        this.hoursMap = new ConcurrentHashMap<>();
        this.objectMapper = new ObjectMapper();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @PostConstruct
    public void start() throws ExecutionException, InterruptedException {
        updateHours();
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000 * 60 * 60 * 24) // Once a day
    public void updateHours() throws ExecutionException, InterruptedException {
        log.info("Updating Epic Universe park hours...");

        OpenAIClientAsync client = openAIService.getClient();

        String s= """
                            Go to: https://www.universalorlando.com/web/en/us/plan-your-visit/hours-information/park-hours/epic-universe
                            
                            Extract all Epic Universe preview hours from **April 17, 2025 to May 10, 2025**.

                            Return the result as a JSON array with this structure:
                            [
                              {
                                "date": "YYYY-MM-DD",
                                "day": "DayOfWeek",
                                "hours": "HH:MM AM/PM - HH:MM AM/PM",
                                "type": "Preview"
                              }
                            ]
                            Only include dates that have valid hours listed.
                            """;

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .maxTokens(500)
                .addUserMessage(s).build();

        ChatCompletion chatCompletion = client.chat().completions().create(params).get();
        CompletableFuture<ChatCompletion> future = client.chat().completions().create(params);

        future.whenComplete((response, throwable) -> {
            if (throwable != null) {
                log.warning("Failed to fetch park hours: " + throwable.getMessage());
                return;
            }

            try {
                Optional<String> content = response.choices().get(0).message().content();
                if (content.isEmpty()) {
                    log.severe("No content returned from GPT.");
                    return;
                }

                List<Map<String, String>> list = objectMapper.readValue(
                        content.get(),
                        new TypeReference<>() {}
                );

                Map<Date, String> updatedMap = new ConcurrentHashMap<>();

                for (Map<String, String> entry : list) {
                    updatedMap.put(Date.valueOf(entry.get("date")), entry.get("hours"));
                }

                hoursMap.clear();
                hoursMap.putAll(updatedMap);

                log.info("Successfully updated Epic Universe hours with " + updatedMap.size() + " entries.");
            } catch (Exception e) {
                log.warning("Error parsing GPT response: " + e.getMessage());
            }
        });
    }


}

