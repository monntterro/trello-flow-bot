package com.monntterro.trelloflowbot.core.config;

import com.github.scribejava.apis.TrelloApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthConfig {
    @Value("${trello.api.key}")
    private String apiKey;
    @Value("${trello.api.secret}")
    private String apiSecret;
    @Value("${app.url}")
    private String appUrl;
    @Value("${trello.api.path}")
    private String path;

    @Bean
    public OAuth10aService oauth10aService() {
        String callbackUrl = appUrl + path;
        return new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .callback(callbackUrl)
                .build(TrelloApi.instance());
    }
}
