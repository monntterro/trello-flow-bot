package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackData;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TrelloModelService;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.service.bot.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.trello.TrelloAccountService;
import com.monntterro.trelloflowbot.bot.service.trello.TrelloClientFacade;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.bot.utils.TelegramMessage;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Service
@RequiredArgsConstructor
public class CallbackHandler {
    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;
    private final TrelloClientFacade trelloClientFacade;
    private final TrelloModelService trelloModelService;
    private final MessageResource messageResource;
    private final TrelloAccountService accountService;

    public void handle(CallbackQuery callbackQuery) {
        String callbackQueryData = callbackQuery.getData();
        if (!dataCache.contains(callbackQueryData)) {
            unsupportedMessage(callbackQuery);
            return;
        }

        String jsonData = dataCache.getAndRemove(callbackQueryData);
        CallbackData callbackData = CallbackData.from(jsonData);
        switch (callbackData.getCallbackType()) {
            case MENU -> menu(callbackQuery);
            case MY_BOARDS -> getMyBoards(callbackQuery);
            case GET_BOARD -> getBoard(callbackQuery, callbackData.getData());
            case SUBSCRIBE -> subscribeToModel(callbackQuery, callbackData.getData());
            case UNSUBSCRIBE -> unsubscribeFromModel(callbackQuery, callbackData.getData());
            case ACCOUNT_SETTINGS -> accountSettings(callbackQuery);
            case LOGOUT -> logout(callbackQuery);
            case LOGOUT_PREPARE -> logoutPrepare(callbackQuery);
            case SETTINGS -> setting(callbackQuery);
        }
    }

    private void logoutPrepare(CallbackQuery callbackQuery) {
        String text = messageResource.getMessage("account.logout.prepare");
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();
        String logoutCallbackData = JsonParser.create().with("type", CallbackType.LOGOUT).toJson();
        String settingsCallbackDataId = dataCache.put(settingsCallbackData);
        String logoutCallbackDataId = dataCache.put(logoutCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("account.logout"), logoutCallbackDataId)),
                row(button(messageResource.getMessage("button.back"), settingsCallbackDataId))
        );
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void logout(CallbackQuery callbackQuery) {
        long userTelegramId = callbackQuery.getFrom().getId();
        accountService.removeAccount(userTelegramId);

        String menuCallbackData = JsonParser.create().with("type", CallbackType.MENU).toJson();
        String menuCallbackDataId = dataCache.put(menuCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("button.back"), menuCallbackDataId))
        );

        String text = messageResource.getMessage("account.logout.success");
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void accountSettings(CallbackQuery callbackQuery) {
        String text = messageResource.getMessage("account.text");
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();

        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();
        String logoutCallbackData = JsonParser.create().with("type", CallbackType.LOGOUT_PREPARE).toJson();

        String settingsCallbackDataId = dataCache.put(settingsCallbackData);
        String logoutCallbackDataId = dataCache.put(logoutCallbackData);

        String url;
        try {
            url = accountService.getLoginUrl(callbackQuery.getFrom().getId());
        } catch (Exception e) {
            bot.sendMessage(messageResource.getMessage("error.text"), chatId);
            return;
        }
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(urlButton(messageResource.getMessage("account.login"), url)),
                row(button(messageResource.getMessage("account.logout"), logoutCallbackDataId)),
                row(button(messageResource.getMessage("button.back"), settingsCallbackDataId))
        );
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void menu(CallbackQuery callbackQuery) {
        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();

        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        String settingsCallbackDataId = dataCache.put(settingsCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.boards"), myBoardsCallbackDataId)),
                row(button(messageResource.getMessage("menu.settings"), settingsCallbackDataId))
        );

        String text = messageResource.getMessage("menu.text");
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void setting(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();

        String accountCallbackData = JsonParser.create().with("type", CallbackType.ACCOUNT_SETTINGS).toJson();
        String menuCallbackData = JsonParser.create().with("type", CallbackType.MENU).toJson();
        String accountCallbackDataId = dataCache.put(accountCallbackData);
        String menuCallbackDataId = dataCache.put(menuCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.account"), accountCallbackDataId)),
                row(button(messageResource.getMessage("button.back"), menuCallbackDataId))
        );

        String text = messageResource.getMessage("settings.text");
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }


    private void unsupportedMessage(CallbackQuery callbackQuery) {
        String text = messageResource.getMessage("callback.unsupported");
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId);
    }

    private void unsubscribeFromModel(CallbackQuery callbackQuery, String data) {
        User user = getUser(callbackQuery);

        int messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();
        boolean isSuccess;
        try {
            String modelId = JsonParser.read(data, "modelId", String.class);
            isSuccess = trelloClientFacade.unsubscribeFromModel(modelId, user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user, messageId);
            return;
        }
        if (!isSuccess) {
            String text = messageResource.getMessage("user.unsubscribe.error");
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);

        String text = messageResource.getMessage("user.unsubscribe.success");
        InlineKeyboardMarkup markup = inlineKeyboard(row(button(messageResource.getMessage("button.back"), myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void subscribeToModel(CallbackQuery callbackQuery, String data) {
        User user = getUser(callbackQuery);
        int messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();

        boolean isSuccess;
        try {
            String modelId = JsonParser.read(data, "modelId", String.class);
            String webhookPath = String.valueOf(user.getId());
            isSuccess = trelloClientFacade.subscribeToModel(modelId, webhookPath, user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user, messageId);
            return;
        }

        if (!isSuccess) {
            String text = messageResource.getMessage("user.subscribe.error");
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);

        String text = messageResource.getMessage("user.subscribe.success");
        InlineKeyboardMarkup markup = inlineKeyboard(row(button(messageResource.getMessage("button.back"), myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "url", String.class);
        String boardName = JsonParser.read(data, "name", String.class);
        String text = messageResource.getMessage("menu.chosen.board", boardName);
        List<MessageEntity> messageEntities = List.of(TelegramMessage.textLink(boardName, boardUrl, text.length() - boardName.length()));

        User user = getUser(callbackQuery);
        String modelId = JsonParser.read(data, "modelId", String.class);
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new RuntimeException("Trello model not found"));

        List<InlineKeyboardRow> rows = new ArrayList<>();
        if (trelloModel.isSubscribed()) {
            rows.add(row(unsubscribeButton(modelId)));
        } else {
            rows.add(row(subscribeButton(modelId)));
        }

        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back"), myBoardsCallbackDataId)));

        InlineKeyboardMarkup markup = inlineKeyboard(rows);
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, messageEntities, markup);
    }

    private InlineKeyboardButton unsubscribeButton(String modelId) {
        String unsubscribeCallbackData = JsonParser.create()
                .with("type", CallbackType.UNSUBSCRIBE)
                .with("modelId", modelId)
                .toJson();
        String unsubscribeCallbackDataId = dataCache.put(unsubscribeCallbackData);
        return button(messageResource.getMessage("model.unsubscribe"), unsubscribeCallbackDataId);
    }

    private InlineKeyboardButton subscribeButton(String modelId) {
        String subscribeCallbackData = JsonParser.create()
                .with("type", CallbackType.SUBSCRIBE)
                .with("modelId", modelId)
                .toJson();
        String subscribeCallbackDataId = dataCache.put(subscribeCallbackData);
        return button(messageResource.getMessage("model.subscribe"), subscribeCallbackDataId);
    }

    private void getMyBoards(CallbackQuery callbackQuery) {
        User user = getUser(callbackQuery);
        int messageId = callbackQuery.getMessage().getMessageId();

        if (user.getToken() == null || user.getToken().isBlank()) {
            String text = messageResource.getMessage("user.not.registered");
            long chatId = callbackQuery.getMessage().getChatId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        List<TrelloModel> userBoards;
        try {
            userBoards = trelloClientFacade.getUserBoards(user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user, messageId);
            return;
        }
        List<InlineKeyboardRow> rows = userBoards.stream()
                .sorted(Comparator.comparing(TrelloModel::isSubscribed).reversed())
                .map(board -> {
                    String callbackData = JsonParser.create()
                            .with("type", CallbackType.GET_BOARD)
                            .with("modelId", board.getModelId())
                            .with("name", board.getName())
                            .with("url", board.getUrl())
                            .toJson();
                    String callbackDataId = dataCache.put(callbackData);
                    String text = "%s %s".formatted(board.getName(), board.isSubscribed() ? "✅" : "❌");
                    return row(button(text, callbackDataId));
                })
                .collect(Collectors.toList());

        String menuCallbackData = JsonParser.create().with("type", CallbackType.MENU).toJson();
        String menuCallbackDataId = dataCache.put(menuCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back"), menuCallbackDataId)));

        String text = messageResource.getMessage("menu.choose.board");
        long chatId = callbackQuery.getMessage().getChatId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
    }

    private void sendEditAuthenticationErrorMessage(User user, int messageId) {
        String text = messageResource.getMessage("user.authentication.error");
        bot.editMessage(text, user.getChatId(), messageId);
    }

    private User getUser(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        return userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
    }
}
