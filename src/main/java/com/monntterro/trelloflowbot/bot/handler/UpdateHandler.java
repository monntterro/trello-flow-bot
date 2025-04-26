package com.monntterro.trelloflowbot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;

    public void handle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler.handle(update.getMessage());
        }
    }
}
