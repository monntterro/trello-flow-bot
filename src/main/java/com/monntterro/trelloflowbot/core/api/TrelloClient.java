package com.monntterro.trelloflowbot.core.api;

import com.monntterro.trelloflowbot.core.model.Board;
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

    public void subscribeToModel(String modelId, String webhookId, String key, String token) {
        String callbackUrl = webhookBaseUrl + "/" + webhookId;
        apiClient.createWebhook(callbackUrl, modelId, key, token);
    }
}
