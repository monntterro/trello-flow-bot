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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

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

    public void handle(CallbackQuery callbackQuery) {
        String callbackQueryData = callbackQuery.getData();
        if (!dataCache.contains(callbackQueryData)) {
            unsupportedMessage(callbackQuery);
            return;
        }
        String jsonData = dataCache.getAndRemove(callbackQueryData);
        CallbackData callbackData = CallbackData.from(jsonData);

        switch (callbackData.getCallbackType()) {
            case MY_BOARDS -> getMyBoards(callbackQuery);
            case GET_BOARD -> getBoard(callbackQuery, callbackData.getData());
            case SUBSCRIBE -> subscribeToModel(callbackQuery, callbackData.getData());
            case UNSUBSCRIBE -> unsubscribeFromModel(callbackQuery, callbackData.getData());
        }
    }

    private void unsupportedMessage(CallbackQuery callbackQuery) {
        String text = "Это сообщение уже не поддерживается, воспользуйтесь командой /menu.";
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
            String text = "Вы не подписаны на эту доску. Подписаться можно в меню.";
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String text = "Вы отписались от доски. Теперь вы не будете получать уведомления об изменениях на этой доске.";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();

        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button("Назад", myBoardsCallbackDataId)));
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
            String text = "Вы уже подписаны на эту доску. Отписаться можно в меню.";
            long chatId = callbackQuery.getMessage().getChatId();
            int messageId = callbackQuery.getMessage().getMessageId();
            bot.editMessage(text, chatId, messageId);
            return;
        }

        String text = "Вы подписались на доску. Теперь вы будет получать уведомление об изменениях на этой доске.";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();

        Bucket bucket = dataCache.createBucket();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button("Назад", myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "url", String.class);
        String boardName = JsonParser.read(data, "name", String.class);
        String text = "Выбранная доска: " + boardName;
        List<MessageEntity> messageEntities = List.of(textLink(boardName, boardUrl, 17));

        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
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
            rows.add(row(button("Отписаться", unsubscribeCallbackDataId)));
        } else {
            String subscribeCallbackData = JsonParser.create()
                    .with("type", CallbackType.SUBSCRIBE)
                    .with("modelId", modelId)
                    .toJson();
            String subscribeCallbackDataId = bucket.put(subscribeCallbackData);
            rows.add(row(button("Подписаться", subscribeCallbackDataId)));
        }
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = bucket.put(myBoardsCallbackData);
        rows.add(row(button("Назад к доскам", myBoardsCallbackDataId)));

        InlineKeyboardMarkup markup = inlineKeyboard(rows);
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, messageEntities, markup);
    }

    private void getMyBoards(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        Bucket bucket = dataCache.createBucket();
        List<InlineKeyboardRow> rows = trelloClientFacade.getUserBoards(user).stream()
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
                .toList();

        String text = "Выберите доску:";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
    }
}
