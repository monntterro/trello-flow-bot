package com.monntterro.trelloflowbot.bot.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("singleton")
public class CallbackDataCache {
    private final ConcurrentHashMap<String, CacheData> cache = new ConcurrentHashMap<>();

    @Value("${telegram.callback.expiration}")
    private Duration expiration;

    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public String put(String data) {
        CacheData cacheData = new CacheData(data);
        String key = UUID.randomUUID().toString();
        cache.put(key, cacheData);
        return key;
    }

    public String getAndRemove(String key) {
        return cache.remove(key).getData();
    }

    @Scheduled(fixedDelayString = "${telegram.callback.cleanup.interval.secs:60}", timeUnit = TimeUnit.SECONDS)
    private void cleanUp() {
        LocalDateTime now = LocalDateTime.now();
        cache.entrySet().removeIf(
                entry -> entry.getValue().getCreatedAt().plusSeconds(expiration.getSeconds()).isBefore(now)
        );
    }
}
