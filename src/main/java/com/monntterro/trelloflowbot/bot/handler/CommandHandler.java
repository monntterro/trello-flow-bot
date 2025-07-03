package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.integration.TrelloAccountService;
import com.monntterro.trelloflowbot.bot.model.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
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
        String myOrganizationsCallbackData = JsonParser.create().with("type", CallbackType.MY_ORGANIZATIONS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();

        String myOrganizationsCallbackDataId = dataCache.put(myOrganizationsCallbackData);
        String settingsCallbackDataId = dataCache.put(settingsCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.organizations"), myOrganizationsCallbackDataId)),
                row(button(messageResource.getMessage("menu.settings"), settingsCallbackDataId))
        );

        String text = messageResource.getMessage("menu.text");
        long chatId = message.getChatId();
        bot.sendMessage(text, chatId, markup);
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