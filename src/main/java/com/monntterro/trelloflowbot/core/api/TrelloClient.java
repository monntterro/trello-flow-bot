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

    /**
     * Validates Trello API credentials.
     *
     * @param key   Trello API key
     * @param token Trello user token
     * @return true if credentials are valid, false otherwise
     */
    public boolean isValidKeyAndToken(String key, String token) {
        try {
            apiClient.getMyBoards(key, token);
            return true;
        } catch (FeignException.Unauthorized e) {
            return false;
        }
    }

    /**
     * Retrieves boards for authenticated user.
     *
     * @param key   Trello API key
     * @param token Trello user token
     * @return List of user's boards
     * @throws AuthenticationException if authentication fails
     */
    public List<Board> getMyBoards(String key, String token) throws AuthenticationException {
        try {
            return apiClient.getMyBoards(key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException();
        }
    }

    /**
     * Creates a webhook subscription for a Trello model.
     *
     * @param modelId     ID of the Trello model to subscribe to
     * @param webhookPath Path endpoint for webhook callbacks
     * @param key         Trello API key
     * @param token       Trello user token
     * @return Created webhook
     * @throws AuthenticationException if authentication fails
     */
    public Webhook subscribeToModel(String modelId, String webhookPath, String key,
                                    String token) throws AuthenticationException {
        String callbackUrl = buildCallbackUrl(webhookPath);
        try {
            return apiClient.createWebhook(callbackUrl, modelId, key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException();
        }
    }

    /**
     * Removes a webhook subscription.
     *
     * @param webhookId ID of the webhook to delete
     * @param key       Trello API key
     * @param token     Trello user token
     * @throws AuthenticationException if authentication fails
     */
    public void unsubscribeFromModel(String webhookId, String key, String token) throws AuthenticationException {
        try {
            apiClient.deleteWebhook(webhookId, key, token);
        } catch (FeignException.Unauthorized e) {
            throw new AuthenticationException();
        }
    }

    private String buildCallbackUrl(String webhookPath) {
        return webhookBaseUrl + "/" + webhookPath;
    }
}
