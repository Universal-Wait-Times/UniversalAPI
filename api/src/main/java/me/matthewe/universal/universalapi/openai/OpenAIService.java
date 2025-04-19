package me.matthewe.universal.universalapi.openai;

import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import com.openai.client.okhttp.OpenAIOkHttpClientAsync;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Log
@Service
@Getter
public class OpenAIService {
    private OpenAIClientAsync client;


    @PostConstruct
    public void start() {
        this.client = OpenAIOkHttpClientAsync.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .build();
        log.info("OpenAI client Started!");
    }
}
