package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final UserService userService;
    private final CommandHandler commandHandler;

    public void handle(Message message) {
        User user = getOrCreateUserFromMessage(message);
        switch (user.getState()) {
            case IDLE -> {
                if (message.isCommand()) {
                    commandHandler.processCommand(message);
                }
            }
        }
    }

    private User getOrCreateUserFromMessage(Message message) {
        long telegramId = message.getFrom().getId();
        long chatId = message.getChatId();

        return userService.findByTelegramId(telegramId)
                .map(user -> updateUserChatId(user, chatId))
                .orElseGet(() -> createNewUser(telegramId, chatId));
    }

    private User updateUserChatId(User user, long chatId) {
        if (user.getChatId() != chatId) {
            user.setChatId(chatId);
            userService.save(user);
        }
        return user;
    }

    private User createNewUser(long telegramId, long chatId) {
        User newUser = User.builder()
                .telegramId(telegramId)
                .chatId(chatId)
                .state(State.IDLE)
                .build();
        return userService.save(newUser);
    }
}
