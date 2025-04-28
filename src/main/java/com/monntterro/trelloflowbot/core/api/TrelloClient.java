package com.monntterro.trelloflowbot.core.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloClient {
    private final TrelloApiClient apiClient;

    public boolean isValidKeyAndToken(String key, String token) {
        try {
            apiClient.getBoards(key, token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
