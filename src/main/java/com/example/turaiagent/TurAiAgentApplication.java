package com.example.turaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TurAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurAiAgentApplication.class, args);
    }

}
