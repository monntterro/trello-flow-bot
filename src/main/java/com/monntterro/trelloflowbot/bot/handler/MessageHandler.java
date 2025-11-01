package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.entity.user.UserSettings;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;

@Component
@RequiredArgsConstructor
public class MessageHandler {
    private final UserService userService;
    private final CommandHandler commandHandler;
    private final TelegramBot bot;
    private final MessageResource messageResource;

    public void handle(Message message) {
        User user = getOrCreateUserFromMessage(message);
        switch (user.getState()) {
            case IDLE -> {
                if (message.isCommand()) {
                    commandHandler.processCommand(message);
                }
            }
            case CHANGE_UTC_OFFSET -> {
                if ("/cancel".equals(message.getText())) {
                    commandHandler.processCommand(message);
                    return;
                }
                changeUtcOffset(message);
            }
        }
    }

    private void changeUtcOffset(Message message) {
        Long chatId = message.getChatId();

        String offset = message.getText();
        if (!offset.matches("^(?:\\+(?:1[0-4]|[0-9])|0|-(?:1[0-2]|[0-9]))$")) {
            String text = messageResource.getMessage("account.change-utc-offset.error");
            bot.sendMessage(text, chatId);
            return;
        }

        Integer utcOffset = Integer.parseInt(offset);
        User user = userService.findByTelegramId(message.getFrom().getId())
                .orElseThrow();
        user.getUserSettings().setUtcOffset(utcOffset);
        user.setState(State.IDLE);
        userService.save(user);

        String text = messageResource.getMessage("account.change-utc-offset.success");
        bot.sendMessage(text, chatId);
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
        UserSettings userSettings = UserSettings.builder()
                .utcOffset(0)
                .build();
        User newUser = User.builder()
                .telegramId(telegramId)
                .chatId(chatId)
                .state(State.IDLE)
                .userSettings(userSettings)
                .build();
        return userService.save(newUser);
    }
}
