package com.monntterro.trelloflowbot.bot.handler;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.model.callback.CallbackData;
import com.monntterro.trelloflowbot.bot.model.callback.Type;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.core.api.TrelloClient;
import com.monntterro.trelloflowbot.core.model.Board;
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
    private final TrelloClient trelloClient;
    private final TelegramBot bot;
    private final UserService userService;
    private final CallbackDataCache dataCache;

    public void handle(CallbackQuery callbackQuery) {
        String jsonData = dataCache.get(callbackQuery.getData());
        CallbackData callbackData = CallbackData.from(jsonData);

        switch (callbackData.getType()) {
            case MY_BOARDS -> getMyBoards(callbackQuery);
            case GET_BOARD -> getBoard(callbackQuery, callbackData.getData());
        }
    }

    private void getBoard(CallbackQuery callbackQuery, String data) {
        String boardUrl = JsonParser.read(data, "url", String.class);
        String boardName = JsonParser.read(data, "name", String.class);
        String text = "Хотите подписаться на доску [%s](%s)?".formatted(boardName, boardUrl);

        String boardId = JsonParser.read(data, "id", String.class);
        String subscribeCallbackData = JsonParser.create()
                .with("type", Type.SUBSCRIBE)
                .with("id", boardId)
                .toJson();
        String subscribeCallbackDataId = dataCache.put(subscribeCallbackData);
        String myBoardsCallbackData = JsonParser.create()
                .with("type", Type.MY_BOARDS)
                .toJson();
        String myBoardsCallbackDataId = dataCache.put(myBoardsCallbackData);
        InlineKeyboardMarkup markup = inlineKeyboard(
                row(button("Подписаться", subscribeCallbackDataId)),
                row(button("К доскам", myBoardsCallbackDataId))
        );

        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, markup);
    }

    private void getMyBoards(CallbackQuery callbackQuery) {
        long telegramId = callbackQuery.getFrom().getId();
        User user = userService.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User with telegramId %d not found".formatted(telegramId)));
        List<Board> boards = trelloClient.getMyBoards(user.getTrelloApiKey(), user.getTrelloApiToken());

        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (Board board : boards) {
            String callbackData = JsonParser.create()
                    .with("type", Type.GET_BOARD)
                    .with("id", board.getId())
                    .with("name", board.getName())
                    .with("url", board.getShortUrl())
                    .toJson();
            String callbackDataId = dataCache.put(callbackData);
            rows.add(row(button(board.getName(), callbackDataId)));
        }

        String text = "Выберите доску:";
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        bot.editMessage(text, chatId, messageId, inlineKeyboard(rows));
    }
}
