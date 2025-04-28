package com.monntterro.trelloflowbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class TrelloFlowBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrelloFlowBotApplication.class, args);
    }
}
