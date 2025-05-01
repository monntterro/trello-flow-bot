package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.cache.Bucket;
import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;

    public void processCommand(Message message) {
        String command = message.getText();
        switch (command) {
            case "/start" -> startCommand(message);
            case "/registration" -> registrationCommand(message);
            case "/cancel" -> cancelCommand(message);
            case "/menu" -> menuCommand(message);
        }
    }

    private void menuCommand(Message message) {
        String text = "Меню";
        String callbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        Bucket bucket = dataCache.createBucket();
        String callbackDataId = bucket.put(callbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(row(button("Мои доски", callbackDataId)));
        bot.sendMessage(text, message.getChatId(), markup);
    }

    private void cancelCommand(Message message) {
        long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
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
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        user.setState(State.REGISTRATION);
        userService.save(user);

        String text = "Пришли мне ключ и через запятую токен Trello API. Для отмены пришли команду /cancel";
        bot.sendMessage(text, message.getChatId());
    }
}