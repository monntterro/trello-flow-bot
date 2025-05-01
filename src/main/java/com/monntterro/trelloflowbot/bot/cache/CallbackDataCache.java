package com.monntterro.trelloflowbot.bot.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Scope("singleton")
public class CallbackDataCache {
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Value("${telegram.callback.expiration}")
    private Duration expiration;

    public Bucket createBucket() {
        String cacheKey = generateKey();
        Bucket bucket = new Bucket(cacheKey);
        cache.put(cacheKey, bucket);
        executor.scheduleWithFixedDelay(this::cleanUp, expiration.getSeconds(), expiration.getSeconds(), TimeUnit.SECONDS);
        return bucket;
    }

    public boolean contains(String key) {
        String[] cacheAndBucketKey = key.split("\\s");
        String cacheKey = cacheAndBucketKey[0];

        return cache.get(cacheKey) != null;
    }

    public String getAndRemove(String key) {
        String[] cacheAndBucketKey = key.split("\\s");
        String cacheKey = cacheAndBucketKey[0];
        String bucketKey = cacheAndBucketKey[1];

        Bucket bucket = cache.remove(cacheKey);
        return bucket.getAndRemove(bucketKey);
    }

    private String generateKey() {
        return UUID.randomUUID().toString().substring(0, 25);
    }

    private void cleanUp() {
        LocalDateTime now = LocalDateTime.now();
        cache.entrySet().removeIf(entry -> {
            Bucket bucket = entry.getValue();
            return bucket.getCreatedAt().plusSeconds(expiration.getSeconds()).isBefore(now);
        });
    }
}
