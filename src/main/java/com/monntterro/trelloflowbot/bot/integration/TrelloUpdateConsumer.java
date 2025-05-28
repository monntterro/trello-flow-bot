package com.monntterro.trelloflowbot.bot.integration;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.TelegramMessage;
import com.monntterro.trelloflowbot.core.model.Data;
import com.monntterro.trelloflowbot.core.model.TranslationKey;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import com.monntterro.trelloflowbot.core.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloUpdateConsumer implements UpdateConsumer {
    private final TelegramBot bot;
    private final UserService userService;

    @Override
    public void consume(TrelloUpdate trelloUpdate, String webhookId) {
        if (trelloUpdate.getAction().getDisplay().getTranslationKey() == TranslationKey.UNKNOWN) {
            return;
        }

        User user = userService.findById(Long.parseLong(webhookId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        notifyUser(user, trelloUpdate);
    }

    public void notifyUser(User user, TrelloUpdate trelloUpdate) {
        TelegramMessage telegramMessage = TelegramMessage.create();

        appendHeader(telegramMessage, trelloUpdate);
        appendUserInfo(telegramMessage, trelloUpdate);
        appendAction(telegramMessage, trelloUpdate);

        bot.sendMessage(telegramMessage.getText(), user.getChatId(), telegramMessage.getEntities());
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
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST:
                appendCardMoveAction(text, trelloUpdate);
                break;
            case ACTION_COMMENT_ON_CARD:
                appendCommentAction(text, trelloUpdate);
                break;
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