package me.matthewe.universal.universalapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UniversalApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniversalApiApplication.class, args);
    }

}
