package com.monntterro.trelloflowbot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UpdateHandler {
    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    public BotApiMethod<?> handle(Update update) {
        System.out.println("------");
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler.handle(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update.getCallbackQuery());
        }
        return null;
    }
}
