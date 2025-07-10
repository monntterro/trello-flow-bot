package com.monntterro.trelloflowbot.bot.integration.consumer;

import com.monntterro.trelloflowbot.bot.cache.CallbackDataCache;
import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.model.CallbackType;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.utils.JsonParser;
import com.monntterro.trelloflowbot.bot.utils.TelegramMessage;
import com.monntterro.trelloflowbot.core.model.Data;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Service
@RequiredArgsConstructor
public class NotificationSender {
    private final TelegramBot bot;
    private final CallbackDataCache dataCache;

    public void send(Reminder reminder) {
        TelegramMessage message = TelegramMessage.create();
        TrelloUpdate trelloUpdate = reminder.getTrelloUpdate();
        appendHeader(message, trelloUpdate);
        appendUserInfo(message, trelloUpdate);
        appendAction(message, trelloUpdate);

        bot.sendMessage(message.getText(), reminder.getUserChatId(), message.getEntities(), buttons(reminder));
    }

    private void appendHeader(TelegramMessage text, TrelloUpdate trelloUpdate) {
        text.append("📌 Обновление на доске: ")
                .textLink(trelloUpdate.getAction().getData().getBoard().getName(),
                          trelloUpdate.getModel().getShortUrl())
                .append("\n\n");
    }

    private void appendUserInfo(TelegramMessage text, TrelloUpdate trelloUpdate) {
        text.append("👤 ")
                .bold(trelloUpdate.getAction().getMemberCreator().getFullName())
                .append("\n");
    }

    private void appendAction(TelegramMessage text, TrelloUpdate trelloUpdate) {
        switch (trelloUpdate.getAction().getDisplay().getTranslationKey()) {
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST -> appendCardMoveAction(text, trelloUpdate);
            case ACTION_COMMENT_ON_CARD -> appendCommentAction(text, trelloUpdate);
        }
    }

    private void appendCardMoveAction(TelegramMessage text, TrelloUpdate trelloUpdate) {
        Data data = trelloUpdate.getAction().getData();
        text.append("🔄 Переместил(а) карточку ")
                .textLink(data.getCard().getName(), "https://trello.com/c/" + data.getCard().getShortLink())
                .append(":\n\n")
                .italic(data.getListBefore().getName())
                .append(" → ")
                .italic(data.getListAfter().getName());
    }

    private void appendCommentAction(TelegramMessage text, TrelloUpdate trelloUpdate) {
        Data data = trelloUpdate.getAction().getData();
        text.append("💬 Оставил(а) комментарий к карточке ")
                .textLink(data.getCard().getName(), "https://trello.com/c/" + data.getCard().getShortLink())
                .append(":\n\n")
                .italic("“" + data.getText() + "”")
                .append("\n\n");
    }

    private InlineKeyboardMarkup buttons(Reminder reminder) {
        String reminderSeenCallbackData = JsonParser.create()
                .with("type", CallbackType.REMINDER_SEEN)
                .with("reminderId", reminder.getId())
                .toJson();
        String postpone10minutesCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 10)
                .toJson();
        String postpone30minutesCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 30)
                .toJson();
        String postpone1hourCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 60)
                .toJson();
        String postpone3hoursCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 180)
                .toJson();
        String postpone6hoursCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 360)
                .toJson();
        String postpone12hoursCallbackData = JsonParser.create()
                .with("type", CallbackType.POSTPONE_REMINDER)
                .with("reminderId", reminder.getId())
                .with("postponeMinutes", 720)
                .toJson();

        String reminderSeenCallbackDataId = dataCache.put(reminderSeenCallbackData);
        String postpone10minutesCallbackDataId = dataCache.put(postpone10minutesCallbackData);
        String postpone30minutesCallbackDataId = dataCache.put(postpone30minutesCallbackData);
        String postpone1hourCallbackDataId = dataCache.put(postpone1hourCallbackData);
        String postpone3hoursCallbackDataId = dataCache.put(postpone3hoursCallbackData);
        String postpone6hoursCallbackDataId = dataCache.put(postpone6hoursCallbackData);
        String postpone12hoursCallbackDataId = dataCache.put(postpone12hoursCallbackData);
        return inlineKeyboard(
                row(button("10 мин", postpone10minutesCallbackDataId),
                    button("30 мин", postpone30minutesCallbackDataId),
                    button("1 ч", postpone1hourCallbackDataId)),
                row(button("3 ч", postpone3hoursCallbackDataId),
                    button("6 ч", postpone6hoursCallbackDataId),
                    button("12 ч", postpone12hoursCallbackDataId)),
                row(button("Отметить как прочитанное ✅", reminderSeenCallbackDataId))
        );
    }
}
