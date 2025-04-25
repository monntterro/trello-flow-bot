package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final TelegramBot bot;
    private final UserService userService;

    public void handle(Update update) {
        Message message = update.getMessage();
        if (message.isCommand()) {
            switch (message.getText()) {
                case "/start" -> startCommand(update);
                case "/subscribe" -> subscribeCommand(update);
            }
        }
    }

    private void subscribeCommand(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        String text;
        if (userService.existsByChatId(chatId)) {
            text = "Вы уже подписаны на объявления";
        } else {
            if (userService.existsByTelegramId(telegramId)) {
                User user = userService.findById(telegramId).get();
                user.setChatId(chatId);
                userService.save(user);
            } else {
                User user = User.builder()
                        .telegramId(telegramId)
                        .chatId(chatId)
                        .build();
                userService.save(user);
            }
            text = "Вы были подписаны на объявления";
        }

        bot.sendMessage(text, chatId);
    }

    private void startCommand(Update update) {
        String text = "Это бот для уведомления об изменениях в пространстве Trello. Для подписки на обновления введите команду /subscribe";
        long chatId = update.getMessage().getChatId();
        bot.sendMessage(text, chatId);
    }
}
