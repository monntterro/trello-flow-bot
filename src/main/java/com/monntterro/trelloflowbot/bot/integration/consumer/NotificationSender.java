package com.monntterro.trelloflowbot.bot.integration.consumer;

import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.utils.TelegramMessage;
import com.monntterro.trelloflowbot.core.model.Data;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSender {
    private final TelegramBot bot;

    public void send(Reminder reminder) {
        TelegramMessage message = TelegramMessage.create();
        TrelloUpdate trelloUpdate = reminder.getTrelloUpdate();
        appendHeader(message, trelloUpdate);
        appendUserInfo(message, trelloUpdate);
        appendAction(message, trelloUpdate);

        bot.sendMessage(message.getText(), reminder.getUserChatId(), message.getEntities());
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
}
