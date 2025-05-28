package com.monntterro.trelloflowbot.bot.cache;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CacheData {
    private final String data;
    private final LocalDateTime createdAt;

    public CacheData(String data) {
        this.data = data;
        this.createdAt = LocalDateTime.now();
    }
}
