package com.monntterro.trelloflowbot.bot.handler;

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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Service
@RequiredArgsConstructor
public class CallbackHandler {
    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;
    private final TrelloClientFacade trelloClientFacade;
    private final TrelloModelService trelloModelService;

    public void handle(CallbackQuery callbackQuery) {
        String jsonData = dataCache.getAndRemove(callbackQuery.getData());
        CallbackData callbackData = CallbackData.from(jsonData);

        switch (callbackData.getCallbackType()) {
            case MY_BOARDS -> getMyBoards(callbackQuery);
            case GET_BOARD -> getBoard(callbackQuery, callbackData.getData());
            case SUBSCRIBE -> subscribeToModel(callbackQuery, callbackData.getData());
            case UNSUBSCRIBE -> unsubscribeFromModel(callbackQuery, callbackData.getData());
        }
    }

    private void unsubscribeFromModel(CallbackQuery callbackQuery, String data) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        String modelId = JsonParser.read(data, "modelId", String.class);
        trelloClientFacade.unsubscribeFromModel(modelId, user);

        String text = "Вы отписались от доски\\. Теперь вы не будете получать уведомления об изменениях на этой доске\\.";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button("Назад", myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void subscribeToModel(CallbackQuery callbackQuery, String data) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        String modelId = JsonParser.read(data, "modelId", String.class);
        String webhookPath = String.valueOf(user.getId());
        trelloClientFacade.subscribeToModel(modelId, webhookPath, user);

        String text = "Вы подписались на доску\\. Теперь вы будет получать уведомление об изменениях на этой доске\\.";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(row(button("Назад", myBoardsCallbackDataId)));
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "url", String.class);
        String boardName = JsonParser.read(data, "name", String.class);
        String text = "Выбранная доска\\: [%s](%s)".formatted(boardName, boardUrl);

        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));
        String modelId = JsonParser.read(data, "modelId", String.class);
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new RuntimeException("Trello model not found"));

        List<InlineKeyboardRow> rows = new ArrayList<>();
        if (trelloModel.isSubscribed()) {
            String unsubscribeCallbackData = JsonParser.create()
                    .with("type", CallbackType.UNSUBSCRIBE)
                    .with("modelId", modelId)
                    .toJson();
            String unsubscribeCallbackDataId = dataCache.put(unsubscribeCallbackData);
            rows.add(row(button("Отписаться", unsubscribeCallbackDataId)));
        } else {
            String subscribeCallbackData = JsonParser.create()
                    .with("type", CallbackType.SUBSCRIBE)
                    .with("modelId", modelId)
                    .toJson();
            String subscribeCallbackDataId = dataCache.put(subscribeCallbackData);
            rows.add(row(button("Подписаться", subscribeCallbackDataId)));
        }
        String myBoardsCallbackData = JsonParser.create()
                .with("type", CallbackType.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        rows.add(row(button("Назад к доскам", myBoardsCallbackDataId)));

        InlineKeyboardMarkup markup = inlineKeyboard(rows);
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getMyBoards(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User with telegramId %d not found".formatted(telegramId)));

        List<InlineKeyboardRow> rows = trelloClientFacade.getUserBoards(user).stream()
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
                .toList();

        String text = "Выберите доску:";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
    }
}
