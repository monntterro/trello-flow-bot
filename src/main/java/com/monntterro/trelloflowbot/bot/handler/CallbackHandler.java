package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.Bucket;
import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackData;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.TrelloClientFacade;
import com.monntterro.trelloflowbot.bot.service.TrelloModelService;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;
import static com.monntterro.trelloflowbot.bot.utils.MessageUtils.textLink;

@Service
@RequiredArgsConstructor
public class CallbackHandler {
    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;
    private final TrelloClientFacade trelloClientFacade;
    private final TrelloModelService trelloModelService;
    private final MessageResource messageResource;

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
            case CHOOSE_LANGUAGE -> chooseLanguage(callbackQuery);
            case SWITCH_LANGUAGE -> switchLanguage(callbackQuery, callbackData.getData());
            case SETTINGS -> setting(callbackQuery);
        }
    }

    private void menu(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String myBoardsCallbackData = JsonParser.create().with("type", CallbackType.MY_BOARDS).toJson();
        String settingsCallbackData = JsonParser.create().with("type", CallbackType.SETTINGS).toJson();
        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        String settingsCallbackDataId = bucket.put(settingsCallbackData);

        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("menu.my.board", user.getLanguage()), myBoardsCallbackDataId)),
                row(button(messageResource.getMessage("menu.settings", user.getLanguage()), settingsCallbackDataId))
        );

        String text = messageResource.getMessage("menu.text", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void chooseLanguage(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String text = messageResource.getMessage("settings.language.change.choose", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();

        String ruLanguageCallbackData = JsonParser.create()
                .with("type", CallbackType.SWITCH_LANGUAGE)
                .with("language", "ru-RU")
                .toJson();
        String enLanguageCallbackData = JsonParser.create()
                .with("type", CallbackType.SWITCH_LANGUAGE)
                .with("language", "en-US")
                .toJson();
        String menuCallbackData = JsonParser.create()
                .with("type", CallbackType.SETTINGS)
                .toJson();
        Bucket bucket = dataCache.createBucket();
        String ruLanguageCallbackDataId = bucket.put(ruLanguageCallbackData);
        String enLanguageCallbackDataId = bucket.put(enLanguageCallbackData);
        String menuCallbackDataId = bucket.put(menuCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button("Русский", ruLanguageCallbackDataId)),
                row(button("English", enLanguageCallbackDataId)),
                row(button(messageResource.getMessage("button.back", user.getLanguage()), menuCallbackDataId))
        );

        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void setting(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String text = messageResource.getMessage("settings.text", user.getLanguage());
        String chooseLanguageCallbackData = JsonParser.create()
                .with("type", CallbackType.CHOOSE_LANGUAGE)
                .toJson();
        String menuCallbackData = JsonParser.create()
                .with("type", CallbackType.MENU)
                .toJson();

        Bucket bucket = dataCache.createBucket();
        String chooseLanguageCallbackDataId = bucket.put(chooseLanguageCallbackData);
        String menuCallbackDataId = bucket.put(menuCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button(messageResource.getMessage("settings.language.change.text", user.getLanguage()), chooseLanguageCallbackDataId)),
                row(button(messageResource.getMessage("button.back", user.getLanguage()), menuCallbackDataId))
        );

        long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void switchLanguage(CallbackQuery callbackQuery, String data) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String language = JsonParser.read(data, "language", String.class);
        user.setLanguage(language);
        userService.save(user);

        Locale locale = Locale.forLanguageTag(language);
        String languageName = locale.getDisplayName(locale);
        String text = messageResource.getMessage("settings.language.change.success", user.getLanguage(), languageName);

        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.SETTINGS)
                .toJson();
        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button(messageResource.getMessage("button.back", user.getLanguage()), myBoardsCallbackDataId)));

        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void unsupportedMessage(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String text = messageResource.getMessage("callback.unsupported", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId);
    }

    private void unsubscribeFromModel(CallbackQuery callbackQuery, String data) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        String modelId = JsonParser.read(data, "modelId", String.class);
        boolean isSuccess = trelloClientFacade.unsubscribeFromModel(modelId, user);
        if (!isSuccess) {
            String text = messageResource.getMessage("user.unsubscribe.error", user.getLanguage());
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String text = messageResource.getMessage("user.unsubscribe.success", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();

        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button(messageResource.getMessage("button.back", user.getLanguage()), myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void subscribeToModel(CallbackQuery callbackQuery, String data) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String modelId = JsonParser.read(data, "modelId", String.class);
        String webhookPath = String.valueOf(user.getId());
        boolean isSuccess = trelloClientFacade.subscribeToModel(modelId, webhookPath, user);
        if (!isSuccess) {
            String text = messageResource.getMessage("user.subscribe.error", user.getLanguage());
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String text = messageResource.getMessage("user.subscribe.success", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();

        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button(messageResource.getMessage("button.back", user.getLanguage()), myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "url", String.class);
        String boardName = JsonParser.read(data, "name", String.class);

        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String text = messageResource.getMessage("menu.chosen.board", user.getLanguage(), boardName);
        List<MessageEntity> messageEntities = List.of(textLink(boardName, boardUrl, text.length() - boardName.length()));

        String modelId = JsonParser.read(data, "modelId", String.class);
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new RuntimeException("Trello model not found"));

        List<InlineKeyboardRow> rows = new ArrayList<>();
        Bucket bucket = dataCache.createBucket();
        if (trelloModel.isSubscribed()) {
            String unsubscribeCallbackData = JsonParser.create()
                    .with("type", CallbackType.UNSUBSCRIBE)
                    .with("modelId", modelId)
                    .toJson();
            String unsubscribeCallbackDataId = bucket.put(unsubscribeCallbackData);
            rows.add(row(button(messageResource.getMessage("model.unsubscribe", user.getLanguage()), unsubscribeCallbackDataId)));
        } else {
            String subscribeCallbackData = JsonParser.create()
                    .with("type", CallbackType.SUBSCRIBE)
                    .with("modelId", modelId)
                    .toJson();
            String subscribeCallbackDataId = bucket.put(subscribeCallbackData);
            rows.add(row(button(messageResource.getMessage("model.subscribe", user.getLanguage()), subscribeCallbackDataId)));
        }
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back", user.getLanguage()), myBoardsCallbackDataId)));

        InlineKeyboardMarkup markup = inlineKeyboard(rows);
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, messageEntities, markup);
    }

    private void getMyBoards(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        if (user.getTrelloApiKey() == null || user.getTrelloApiKey().isBlank()) {
            String text = messageResource.getMessage("user.not.registered", user.getLanguage());
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        Bucket bucket = dataCache.createBucket();
        List<InlineKeyboardRow> rows = trelloClientFacade.getUserBoards(user).stream()
                .sorted(Comparator.comparing(TrelloModel::isSubscribed).reversed())
                .map(board -> {
                    String callbackData = JsonParser.create()
                            .with("type", CallbackType.GET_BOARD)
                            .with("modelId", board.getModelId())
                            .with("name", board.getName())
                            .with("url", board.getUrl())
                            .toJson();
                    String callbackDataId = bucket.put(callbackData);
                    String text = "%s %s".formatted(board.getName(), board.isSubscribed() ? "✅" : "❌");
                    return row(button(text, callbackDataId));
                })
                .collect(Collectors.toList());
        String menuCallbackData = JsonParser.create()
                .with("type", CallbackType.MENU)
                .toJson();
        String menuCallbackDataId = bucket.put(menuCallbackData);
        rows.add(row(button(messageResource.getMessage("button.back", user.getLanguage()), menuCallbackDataId)));

        String text = messageResource.getMessage("menu.choose.board", user.getLanguage());
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
    }
}
