package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.entity.State;
import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final TelegramBot bot;
    private final UserService userService;

    public void processCommand(Message message) {
        String command = message.getText();
        switch (command) {
            case "/start" -> startCommand(message);
            case "/registration" -> registrationCommand(message);
            case "/cancel" -> cancelCommand(message);
        }
    }

    private void cancelCommand(Message message) {
        long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User with telegramId %d not found".formatted(telegramId)));
        user.setState(State.IDLE);
        userService.save(user);

        String text = "Регистрация отменена.";
        bot.sendMessage(text, message.getChatId());
    }

    private void startCommand(Message message) {
        String text = "Привет!";
        bot.sendMessage(text, message.getChatId());
        registrationCommand(message);
    }

    private void registrationCommand(Message message) {
        long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User with telegramId %d not found".formatted(telegramId)));
        user.setState(State.REGISTRATION);
        userService.save(user);

        String text = "Пришли мне ключ и через запятую токен Trello API. Для отмены пришли команду /cancel";
        bot.sendMessage(text, message.getChatId());
    }
}