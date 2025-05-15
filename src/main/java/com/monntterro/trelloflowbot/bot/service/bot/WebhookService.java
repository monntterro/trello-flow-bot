package com.monntterro.trelloflowbot.bot.service.bot;

import com.monntterro.trelloflowbot.bot.config.props.TelegramBotProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService {
    private final RestTemplate restTemplate;
    private final TelegramBotProperties properties;

    @Value("${app.url}")
    private String appUrl;

    public WebhookService(TelegramBotProperties properties) {
        this.restTemplate = new RestTemplate();
        this.properties = properties;
    }

    public void setWebhook() {
        String webhookUrl = "https://api.telegram.org/bot" + properties.getToken() + "/setWebhook?url=" + appUrl + "/" + properties.getPath();
        restTemplate.postForObject(webhookUrl, null, String.class);
    }

    public void deleteWebhook() {
        String deleteWebhookUrl = "https://api.telegram.org/bot" + properties.getToken() + "/deleteWebhook";
        restTemplate.postForObject(deleteWebhookUrl, null, String.class);
    }
}
