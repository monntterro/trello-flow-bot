package com.monntterro.trelloflowbot.bot.cache;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * The {@code CacheData} class represents a single unit of cached callback data stored in {@link CallbackDataCache}.
 * <p>
 * Each instance contains:
 * <ul>
 *   <li>{@code data} — the actual callback data payload to be associated with a key.</li>
 *   <li>{@code createdAt} — the timestamp indicating when this entry was created.</li>
 * </ul>
 * <p>
 * The {@code createdAt} field is primarily used for determining the age of the data,
 * which allows the storage system to periodically remove expired or unused entries
 * to prevent memory overflow.
 */

@Getter
public class CacheData {
    private final String data;
    private final LocalDateTime createdAt;

    public CacheData(String data) {
        this.data = data;
        this.createdAt = LocalDateTime.now();
    }
}
