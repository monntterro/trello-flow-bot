package com.monntterro.trelloflowbot.bot.cache;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Bucket {
    private final ConcurrentHashMap<String, String> data;
    private final String bucketKey;
    private final LocalDateTime createdAt;

    public Bucket(String bucketKey) {
        this.bucketKey = bucketKey;
        this.data = new ConcurrentHashMap<>();
        this.createdAt = LocalDateTime.now();
    }

    public String put(String json) {
        String key = generateKey();
        data.put(key, json);
        return bucketKey + " " + key;
    }

    public String get(String key) {
        return data.get(key);
    }

    private String generateKey() {
        return UUID.randomUUID().toString().substring(0, 25);
    }
}
