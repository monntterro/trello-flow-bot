package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.entity.Role;
import com.monntterro.trelloflowbot.bot.entity.State;
import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.processor.CommandProcessor;
import com.monntterro.trelloflowbot.bot.processor.RegistrationProcessor;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final CommandProcessor commandProcessor;
    private final RegistrationProcessor registrationProcessor;
    private final UserService userService;

    public void handle(Message message) {
        if (message.isCommand()) {
            if ("/start".equals(message.getText())) {
                commandProcessor.process(message);
                return;
            }
        }

        long chatId = message.getChatId();
        long telegramId = message.getFrom().getId();

        if (userService.existsByTelegramId(telegramId)) {
            if (!userService.existsByChatId(chatId)) {
                userService.updateChatId(telegramId, chatId);
            }
        } else {
            User user = User.builder()
                    .chatId(chatId)
                    .telegramId(telegramId)
                    .role(Role.UNDEFINED)
                    .state(State.NOT_REGISTERED)
                    .build();
            userService.save(user);
            registrationProcessor.authorize(message, user);
            return;
        }

        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User with telegramId %d not found".formatted(telegramId)));
        switch (user.getState()) {
            case IDLE -> {
                if (message.isCommand()) {
                    commandProcessor.process(message);
                }
            }
            case NOT_REGISTERED -> registrationProcessor.authorize(message, user);
            case CHOOSING_ROLE -> registrationProcessor.process(message, user);
        }
    }
}
