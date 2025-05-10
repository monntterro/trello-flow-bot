package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.cache.Bucket;
import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.user.State;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
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
    private static final String START_COMMAND = "/start";
    private static final String SET_TOKEN_COMMAND = "/set_token_and_key";
    private static final String CANCEL_COMMAND = "/cancel";
    private static final String MENU_COMMAND = "/menu";

    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;
    private final MessageResource messageResource;

    public void processCommand(Message message) {
        String command = message.getText();
        switch (command) {
            case START_COMMAND -> startCommand(message);
            case SET_TOKEN_COMMAND -> setTrelloTokenAndKey(message);
            case CANCEL_COMMAND -> cancelCommand(message);
            case MENU_COMMAND -> menuCommand(message);
        }
    }

    private void menuCommand(Message message) {
        String text = messageResource.getMessage("menu.text");
        InlineKeyboardMarkup markup = buildMenuMarkup();
        bot.sendMessage(text, message.getChatId(), markup);
    }

    private InlineKeyboardMarkup buildMenuMarkup() {
        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();
        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        String settingsCallbackDataId = bucket.put(settingsCallbackData);

        return inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.boards"), myBoardsCallbackDataId)),
                row(button(messageResource.getMessage("menu.settings"), settingsCallbackDataId))
        );
    }

    private void cancelCommand(Message message) {
        User user = getUserByTelegramId(message.getFrom().getId());
        user.setState(State.IDLE);
        userService.save(user);
        bot.sendMessage(messageResource.getMessage("settings.token_and_key.set.cancel"), message.getChatId());
    }

    private void startCommand(Message message) {
        bot.sendMessage(messageResource.getMessage("start.text"), message.getChatId());
        setTrelloTokenAndKey(message);
    }

    private void setTrelloTokenAndKey(Message message) {
        User user = getUserByTelegramId(message.getFrom().getId());
        user.setState(State.CHANGE_TRELLO_TOKEN_AND_KEY);
        userService.save(user);
        bot.sendMessageWithMarkdown(messageResource.getMessage("settings.token_and_key.set.text"), message.getChatId());
    }

    private User getUserByTelegramId(long telegramId) {
        return userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}