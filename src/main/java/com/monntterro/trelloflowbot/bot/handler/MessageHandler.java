package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.processor.CommandProcessor;
import com.monntterro.trelloflowbot.bot.processor.RegistrationProcessor;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final UserService userService;
    private final CommandProcessor commandProcessor;
    private final RegistrationProcessor registrationProcessor;

    public void handle(Message message) {
        User user = getOrCreateUserFromMessage(message);
        switch (user.getState()) {
            case IDLE -> {
                if (message.isCommand()) {
                    commandProcessor.processCommand(message);
                }
            }
            case CHANGE_TRELLO_TOKEN_AND_KEY -> registrationProcessor.register(message, user);
        }
    }

    private User getOrCreateUserFromMessage(Message message) {
        long telegramId = message.getFrom().getId();
        long chatId = message.getChatId();

        if (userService.existsByTelegramId(telegramId)) {
            User user = userService.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
            if (user.getChatId() != chatId) {
                userService.updateChatId(telegramId, chatId);
            }
            return userService.save(user);
        } else {
            User user = User.builder()
                    .telegramId(telegramId)
                    .chatId(chatId)
                    .state(State.IDLE)
                    .build();
            return userService.save(user);
        }
    }
}
