package com.monntterro.trelloflowbot.bot.utils;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;

public class MessageUtils {

    public static MessageEntity textLink(String text, String url, int offset) {
        return MessageEntity.builder()
                .type("text_link")
                .url(url)
                .length(text.length())
                .offset(offset)
                .build();
    }

    public static MessageEntity bold(String text, int offset) {
        return MessageEntity.builder()
                .type("bold")
                .length(text.length())
                .offset(offset)
                .build();
    }
}
