package com.monntterro.trelloflowbot.bot.cache;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class CallbackDataCache {
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String put(String json) {
        String key = UUID.randomUUID().toString();
        cache.put(key, json);
        return key;
    }

    public String getAndRemove(String key) {
        return cache.remove(key);
    }
}
