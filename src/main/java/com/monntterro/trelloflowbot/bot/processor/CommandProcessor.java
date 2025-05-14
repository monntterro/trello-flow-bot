package com.monntterro.trelloflowbot.bot.processor;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.monntterro.trelloflowbot.bot.cache.Bucket;
import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.TrelloOAuthSecretStorage;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.core.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Component
@RequiredArgsConstructor
public class CommandProcessor {
    private static final String START_COMMAND = "/start";
    private static final String LOGIN = "/login";
    private static final String MENU_COMMAND = "/menu";

    private final TelegramBot bot;
    private final CallbackDataCache dataCache;
    private final MessageResource messageResource;
    private final OAuthService oAuthService;
    private final TrelloOAuthSecretStorage trelloOAuthSecretStorage;

    public void processCommand(Message message) {
        String command = message.getText();
        switch (command) {
            case START_COMMAND -> startCommand(message);
            case LOGIN -> trelloLogin(message);
            case MENU_COMMAND -> menuCommand(message);
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

    private void startCommand(Message message) {
        bot.sendMessage(messageResource.getMessage("start.text"), message.getChatId());
        trelloLogin(message);
    }

    private void trelloLogin(Message message) {
        long chatId = message.getChatId();

        OAuth1RequestToken requestToken;
        try {
            requestToken = oAuthService.getRequestToken();
            trelloOAuthSecretStorage.put(requestToken.getToken(), requestToken.getTokenSecret(), message.getFrom()
                    .getId());
        } catch (Exception e) {
            bot.sendMessage(messageResource.getMessage("error.text"), chatId);
            return;
        }

        String text = messageResource.getMessage("trello.login.text");
        String url = oAuthService.getAuthorizationUrl(requestToken);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(urlButton(messageResource.getMessage("menu.login"), url))
        );
        bot.sendMessage(text, chatId, markup);
    }
}