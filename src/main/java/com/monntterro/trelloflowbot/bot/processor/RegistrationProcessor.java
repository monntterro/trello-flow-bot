package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.entity.State;
import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.core.api.TrelloClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class RegistrationProcessor {
    private final TelegramBot bot;
    private final UserService userService;
    private final TrelloClient trelloClient;
    private final CommandProcessor commandProcessor;

    public void register(Message message, User user) {
        String messageText = message.getText();
        if (messageText.equals("/cancel")) {
            commandProcessor.processCommand(message);
            return;
        }

        String[] keyAndToken = messageText.split(",\\s+", 2);
        if (keyAndToken.length != 2) {
            String text = "Неверный формат\\. Попробуйте еще раз\\.";
            bot.sendMessage(text, message.getChatId());
            return;
        }

        String key = keyAndToken[0];
        String token = keyAndToken[1];
        if (!trelloClient.isValidKeyAndToken(key, token)) {
            String text = "Неверный ключ или токен\\. Попробуйте еще раз\\.";
            bot.sendMessage(text, message.getChatId());
            return;
        }

        user.setState(State.IDLE);
        user.setTrelloApiKey(key);
        user.setTrelloApiToken(token);
        userService.save(user);
        String text = "Регистрация прошла успешно\\! Теперь вы можете использовать бота\\. Воспользуйся командой /menu, чтобы увидеть доступные команды\\.";
        bot.sendMessage(text, message.getChatId());
    }
}
