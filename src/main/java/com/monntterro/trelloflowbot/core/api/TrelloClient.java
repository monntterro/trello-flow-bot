package com.monntterro.trelloflowbot.core.api;

import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Board;
import com.monntterro.trelloflowbot.core.model.Webhook;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for Trello API operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloClient {
    private final TrelloApiClient apiClient;

    @Value("${trello.webhook.baseUrl}")
    private String webhookBaseUrl;

    public boolean isValidKeyAndToken(String key, String token) {
        try {
            apiClient.getMe(key, token);
            return true;
        } catch (FeignException.Unauthorized e) {
            return false;
        }
    }


    public List<Board> getMyBoards(String key, String token) throws AuthenticationException {
        try {
            return apiClient.getMyBoards(key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException("Invalid Trello API key or token");
        }
    }

    public Webhook subscribeToModel(String modelId, String webhookPath, String key,
                                    String token) throws AuthenticationException {
        String callbackUrl = buildCallbackUrl(webhookPath);
        try {
            return apiClient.createWebhook(callbackUrl, modelId, key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException("Invalid Trello API key or token");
        }
    }


    public void unsubscribeFromModel(String webhookId, String key, String token) throws AuthenticationException {
        try {
            apiClient.deleteWebhook(webhookId, key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException("Invalid Trello API key or token");
        }
    }

    private String buildCallbackUrl(String webhookPath) {
        return webhookBaseUrl + "/" + webhookPath;
    }
}
