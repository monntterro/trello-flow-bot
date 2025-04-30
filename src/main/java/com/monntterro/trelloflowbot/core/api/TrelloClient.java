package com.monntterro.trelloflowbot.core.api;

import com.monntterro.trelloflowbot.core.model.Board;
import com.monntterro.trelloflowbot.core.model.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrelloClient {
    private final TrelloApiClient apiClient;

    @Value("${trello.webhook.baseUrl}")
    private String webhookBaseUrl;

    public boolean isValidKeyAndToken(String key, String token) {
        try {
            apiClient.getMyBoards(key, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Board> getMyBoards(String key, String token) {
        return apiClient.getMyBoards(key, token);
    }

    public Webhook subscribeToModel(String modelId, String webhookPath, String key, String token) {
        String callbackUrl = webhookBaseUrl + "/" + webhookPath;
        return apiClient.createWebhook(callbackUrl, modelId, key, token);
    }

    public void unsubscribeFromModel(String webhookId, String key, String token) {
        apiClient.deleteWebhook(webhookId, key, token);
    }
}
