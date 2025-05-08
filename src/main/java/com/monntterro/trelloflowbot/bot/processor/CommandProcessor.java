package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.cache.Bucket;
import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
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
    private final MessageResource messageResource;

    public void processCommand(Message message) {
        String command = message.getText();
        switch (command) {
            case "/start" -> startCommand(message);
            case "/set_token_and_key" -> setTrelloTokenAndKey(message);
            case "/cancel" -> cancelCommand(message);
            case "/menu" -> menuCommand(message);
        }
    }

    private void menuCommand(Message message) {
        String text = messageResource.getMessage("menu.text");

        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();
        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        String settingsCallbackDataId = bucket.put(settingsCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.boards"), myBoardsCallbackDataId)),
                row(button(messageResource.getMessage("menu.settings"), settingsCallbackDataId))
        );
        bot.sendMessage(text, message.getChatId(), markup);
    }

    private void cancelCommand(Message message) {
        long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        user.setState(State.IDLE);
        userService.save(user);

        String text = messageResource.getMessage("settings.token_and_key.set.cancel");
        bot.sendMessage(text, message.getChatId());
    }

    private void startCommand(Message message) {
        String text = messageResource.getMessage("start.text", "en-US");
        long chatId = message.getChatId();
        bot.sendMessage(text, chatId);

        setTrelloTokenAndKey(message);
    }

    private void setTrelloTokenAndKey(Message message) {
        long telegramId = message.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        user.setState(State.CHANGE_TRELLO_TOKEN_AND_KEY);
        userService.save(user);

        String text = messageResource.getMessage("settings.token_and_key.set.text");
        bot.sendMessageWithMarkdown(text, message.getChatId());
    }
}