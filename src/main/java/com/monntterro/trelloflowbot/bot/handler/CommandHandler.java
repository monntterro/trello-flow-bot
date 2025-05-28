package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.bot.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.trello.TrelloAccountService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Component
@RequiredArgsConstructor
public class CommandHandler {
    private static final String START_COMMAND = "/start";
    private static final String LOGIN = "/login";
    private static final String MENU_COMMAND = "/menu";

    private final TelegramBot bot;
    private final CallbackDataCache dataCache;
    private final MessageResource messageResource;
    private final TrelloAccountService accountService;

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

        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        String settingsCallbackDataId = dataCache.put(settingsCallbackData);
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
        String text = messageResource.getMessage("account.login.text");
        long chatId = message.getChatId();

        String url;
        try {
            accountService.removeAccount(message.getFrom().getId());
            url = accountService.getLoginUrl(message.getFrom().getId());
        } catch (Exception e) {
            bot.sendMessage(messageResource.getMessage("error.text"), chatId);
            return;
        }
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(urlButton(messageResource.getMessage("account.login"), url))
        );
        bot.sendMessage(text, chatId, markup);
    }
}