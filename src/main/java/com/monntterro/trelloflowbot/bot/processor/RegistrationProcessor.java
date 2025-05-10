package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.core.api.TrelloClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class RegistrationProcessor {
    private static final String CANCEL_COMMAND = "/cancel";

    private final TelegramBot bot;
    private final UserService userService;
    private final TrelloClient trelloClient;
    private final CommandProcessor commandProcessor;
    private final MessageResource messageResource;

    public void register(Message message, User user) {
        String messageText = message.getText();
        if (CANCEL_COMMAND.equals(messageText)) {
            commandProcessor.processCommand(message);
            return;
        }

        String[] keyAndToken = messageText.split(",\\s+", 2);
        if (keyAndToken.length < 2) {
            bot.sendMessage(messageResource.getMessage("settings.token_and_key.set.error.wrong_format"), message.getChatId());
            return;
        }

        String key = keyAndToken[0];
        String token = keyAndToken[1];
        if (!trelloClient.isValidKeyAndToken(key, token)) {
            bot.sendMessage(messageResource.getMessage("settings.token_and_key.set.error.invalid"), message.getChatId());
            return;
        }

        updateUserCredentials(user, key, token);
        bot.sendMessage(messageResource.getMessage("settings.token_and_key.set.success"), message.getChatId());
    }

    private void updateUserCredentials(User user, String key, String token) {
        user.setState(State.IDLE);
        user.setTrelloApiKey(key);
        user.setTrelloApiToken(token);
        userService.save(user);
    }
}
