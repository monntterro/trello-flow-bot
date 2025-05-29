package com.monntterro.trelloflowbot.bot.integration;

import com.monntterro.trelloflowbot.bot.model.OAuthSecret;
import com.monntterro.trelloflowbot.core.service.OAuthSecretStorage;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrelloOAuthSecretStorage implements OAuthSecretStorage {
    private final ConcurrentHashMap<Long, OAuthSecret> storage = new ConcurrentHashMap<>();

    public void put(String token, String tokenSecret, long telegramId) {
        storage.put(telegramId, new OAuthSecret(token, tokenSecret));
    }

    @Override
    public Optional<String> getSecretToken(String token) {
        return storage.values().stream()
                .filter(oAuthSecret -> oAuthSecret.getToken().equals(token))
                .map(OAuthSecret::getTokenSecret)
                .findAny();
    }

    public long getKeyByToken(String token) {
        return storage.entrySet().stream()
                .filter(e -> e.getValue().getToken().equals(token))
                .findAny()
                .orElseThrow().getKey();
    }

    public void removeByToken(String token) {
        storage.values().removeIf(oAuthSecret -> oAuthSecret.getToken().equals(token));
    }

    public void removeByToken(long userTelegramId) {
        storage.remove(userTelegramId);
    }
}
