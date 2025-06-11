package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.ListModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.integration.TrelloAccountService;
import com.monntterro.trelloflowbot.bot.integration.TrelloClientFacade;
import com.monntterro.trelloflowbot.bot.model.CallbackData;
import com.monntterro.trelloflowbot.bot.model.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.bot.utils.TelegramMessage;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
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
            case GET_BOARD -> getBoard(callbackQuery, callbackData.getData());
            case MY_ORGANIZATIONS -> myOrganizations(callbackQuery);
            case GET_BOARDS_BY_ORGANIZATION -> getBoardsByOrganization(callbackQuery, callbackData.getData());
            case SUBSCRIBE -> subscribeToList(callbackQuery, callbackData.getData());
            case UNSUBSCRIBE -> unsubscribeFromList(callbackQuery, callbackData.getData());
            case ACCOUNT_SETTINGS -> accountSettings(callbackQuery);
            case LOGOUT -> logout(callbackQuery);
            case LOGOUT_PREPARE -> logoutPrepare(callbackQuery);
            case SETTINGS -> setting(callbackQuery);
        }
    }

    private void getBoardsByOrganization(CallbackQuery callbackQuery, String data) {
        String organizationName = JsonParser.read(data, "organizationName", String.class);
        String organizationUrl = JsonParser.read(data, "organizationUrl", String.class);
        String text = messageResource.getMessage("menu.chosen.organization", organizationName);
        List<MessageEntity> messageEntities = List.of(TelegramMessage.textLink(organizationName, organizationUrl, 24));

        User user = getUser(callbackQuery);
        String organizationId = JsonParser.read(data, "organizationId", String.class);
        List<InlineKeyboardRow> rows = trelloClientFacade.getBoardsByOrganization(organizationId, user).stream()
                .map(board -> {
                    String callbackData = JsonParser.create()
                            .with("type", CallbackType.GET_BOARD)
                            .with("boardId", board.getId())
                            .with("boardName", board.getName())
                            .with("boardUrl", board.getUrl())
                            .with("organizationId", organizationId)
                            .with("organizationName", organizationName)
                            .with("organizationUrl", organizationUrl)
                            .toJson();
                    String callbackDataId = dataCache.put(callbackData);
                    String buttonText = board.getName() + (board.isSubscribed() ? " ✅" : " ❌");
                    return row(button(buttonText, callbackDataId));
                })
                .collect(Collectors.toList());

        String menuCallbackData = JsonParser.create().with("type", CallbackType.MY_ORGANIZATIONS).toJson();
        String menuCallbackDataId = dataCache.put(menuCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back"), menuCallbackDataId)));

        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, messageEntities, inlineKeyboard(rows));
    }

    private void myOrganizations(CallbackQuery callbackQuery) {
        User user = getUser(callbackQuery);
        int messageId = callbackQuery.getMessage().getMessageId();

        if (user.getToken() == null || user.getToken().isBlank()) {
            String text = messageResource.getMessage("user.not.registered");
            long chatId = callbackQuery.getMessage().getChatId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        List<Organization> userOrganizations;
        try {
            userOrganizations = trelloClientFacade.getMyOrganizations(user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user.getChatId(), messageId);
            return;
        }

        List<InlineKeyboardRow> rows = userOrganizations.stream()
                .map(organization -> {
                    String callbackData = JsonParser.create()
                            .with("type", CallbackType.GET_BOARDS_BY_ORGANIZATION)
                            .with("organizationId", organization.getId())
                            .with("organizationName", organization.getDisplayName())
                            .with("organizationUrl", organization.getUrl())
                            .toJson();
                    String callbackDataId = dataCache.put(callbackData);
                    String text = organization.getDisplayName();
                    return row(button(text, callbackDataId));
                })
                .collect(Collectors.toList());

        String menuCallbackData = JsonParser.create().with("type", CallbackType.MENU).toJson();
        String menuCallbackDataId = dataCache.put(menuCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back"), menuCallbackDataId)));

        String text = messageResource.getMessage("menu.choose.organization");
        long chatId = callbackQuery.getMessage().getChatId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
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
        String myOrganizationsCallbackData = JsonParser.create().with("type", CallbackType.MY_ORGANIZATIONS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();

        String myOrganizationsCallbackDataId = dataCache.put(myOrganizationsCallbackData);
        String settingsCallbackDataId = dataCache.put(settingsCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.organizations"), myOrganizationsCallbackDataId)),
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

    private void unsubscribeFromList(CallbackQuery callbackQuery, String data) {
        User user = getUser(callbackQuery);
        int messageId = callbackQuery.getMessage().getMessageId();

        try {
            Long listId = JsonParser.read(data, "listId", Long.class);
            trelloClientFacade.unsubscribeFromList(listId, user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user.getChatId(), messageId);
            return;
        }

        getBoard(callbackQuery, data);
    }

    private void subscribeToList(CallbackQuery callbackQuery, String data) {
        User user = getUser(callbackQuery);
        int messageId = callbackQuery.getMessage().getMessageId();

        try {
            Long listId = JsonParser.read(data, "listId", Long.class);
            trelloClientFacade.subscribeToList(listId, user);
        } catch (AuthenticationException e) {
            sendEditAuthenticationErrorMessage(user.getChatId(), messageId);
            return;
        }

        getBoard(callbackQuery, data);
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "boardUrl", String.class);
        String boardName = JsonParser.read(data, "boardName", String.class);
        String text = messageResource.getMessage("menu.chosen.board", boardName);
        List<MessageEntity> messageEntities = List.of(TelegramMessage.textLink(boardName, boardUrl, 18));

        User user = getUser(callbackQuery);
        Long boardId = JsonParser.read(data, "boardId", Long.class);
        List<ListModel> lists = trelloClientFacade.getListsForBoard(boardId, user);
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (ListModel list : lists) {
            String buttonText = list.getName() + " " + (list.isSubscribed() ? "✅" : "❌");
            String callbackData = JsonParser.create()
                    .with("type", list.isSubscribed() ? CallbackType.UNSUBSCRIBE : CallbackType.SUBSCRIBE)
                    .with("boardId", boardId)
                    .with("boardName", boardName)
                    .with("boardUrl", boardUrl)
                    .with("listId", list.getId())
                    .toJson();
            String callbackDataId = dataCache.put(callbackData);
            rows.add(row(button(buttonText, callbackDataId)));
        }

        String organizationId = JsonParser.read(data, "organizationId", String.class);
        String organizationName = JsonParser.read(data, "organizationName", String.class);
        String organizationUrl = JsonParser.read(data, "organizationUrl", String.class);
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.GET_BOARDS_BY_ORGANIZATION)
                .with("organizationId", organizationId)
                .with("organizationName", organizationName)
                .with("organizationUrl", organizationUrl)
                .toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back"), myBoardsCallbackDataId)));

        InlineKeyboardMarkup markup = inlineKeyboard(rows);
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, messageEntities, markup);
    }

    private void sendEditAuthenticationErrorMessage(long chatId, int messageId) {
        String text = messageResource.getMessage("user.authentication.error");
        bot.editMessage(text, chatId, messageId);
    }

    private User getUser(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        return userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
    }
}
