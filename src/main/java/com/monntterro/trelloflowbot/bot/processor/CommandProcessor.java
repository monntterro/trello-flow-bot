package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final TelegramBot bot;

    public void process(Message message) {
        switch (message.getText()) {
            case "/start" -> startCommand(message);
        }
    }

    private void startCommand(Message message) {
        String text = "Это бот для уведомления об изменениях в пространстве Trello.";
        long chatId = message.getChatId();
        bot.sendMessage(text, chatId);
    }
}
