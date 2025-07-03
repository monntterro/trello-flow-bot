package com.monntterro.trelloflowbot.bot.utils;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TelegramMessage {
    private final StringBuilder text;
    private final List<MessageEntity> entities;

    private TelegramMessage() {
        this.text = new StringBuilder();
        this.entities = new ArrayList<>();
    }

    public static TelegramMessage create() {
        return new TelegramMessage();
    }

    public TelegramMessage append(String content) {
        text.append(content);
        return this;
    }

    public TelegramMessage bold(String content) {
        return addEntity(content, "bold", null);
    }

    public TelegramMessage italic(String content) {
        return addEntity(content, "italic", null);
    }

    public TelegramMessage textLink(String displayText, String url) {
        return addEntity(displayText, "text_link", url);
    }

    private TelegramMessage addEntity(String content, String type, String url) {
        int offset = text.length();
        text.append(content);
        int length = content.length();

        MessageEntity entity = new MessageEntity(type, offset, length);
        if (url != null) {
            entity.setUrl(url);
        }

        entities.add(entity);
        return this;
    }

    public String getText() {
        return text.toString();
    }

    public static MessageEntity textLink(String text, String url, int offset) {
        return MessageEntity.builder()
                .type("text_link")
                .url(url)
                .length(text.length())
                .offset(offset)
                .build();
    }
}
