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

/**
 * The {@code CallbackDataCache} class is responsible for storing and managing callback data associated with unique
 * keys, used in the context of Telegram Bot API.
 * <p>
 * Telegram imposes a strict limit of 64 characters on the {@code callbackData} string attached to inline keyboard
 * buttons. However, in many cases, the data that needs to be passed during a callback exceeds this limit.
 * <p>
 * To overcome this restriction, this class stores the full callback information on the server side and maps it to a
 * short, unique key. Only this key is sent as {@code callbackData}. When the bot receives a callback query, it uses the
 * key to retrieve the full data from this storage.
 * <p>
 * Additionally, the storage performs periodic cleanup to avoid memory overuse from expired or unused callback entries.
 * Since callbacks are only removed from storage upon retrieval, a scheduled cleanup task runs at a configurable
 * interval ({@code #expiration} property), removing stale entries that have not been accessed within a defined
 * expiration time.
 * <p>
 * Typical usage:
 * <pre>
 *     String key = callbackDataStorage.store(fullData);
 *     InlineKeyboardButton button = new InlineKeyboardButton("Action");
 *     button.setCallbackData(key);
 * </pre>
 * <p>
 * This approach ensures that large and complex data structures can be safely associated with Telegram inline buttons
 * without exceeding Telegram’s callback data size limit, while also managing memory efficiently.
 */

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
