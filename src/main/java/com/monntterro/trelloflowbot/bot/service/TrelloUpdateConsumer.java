package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.core.model.Data;
import com.monntterro.trelloflowbot.core.model.TranslationKey;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.ArrayList;
import java.util.List;

import static com.monntterro.trelloflowbot.bot.utils.MessageUtils.bold;
import static com.monntterro.trelloflowbot.bot.utils.MessageUtils.textLink;

@Service
@RequiredArgsConstructor
public class TrelloUpdateConsumer {
    private final TelegramBot bot;
    private final UserService userService;

    public void consume(TrelloUpdate trelloUpdate, String webhookId) {
        if (trelloUpdate.getAction().getDisplay().getTranslationKey() == TranslationKey.UNKNOWN) {
            return;
        }

        User user = userService.findById(Long.parseLong(webhookId))
                .orElseThrow(() -> new RuntimeException("User with id %s not found".formatted(webhookId)));
        notifyUser(user, trelloUpdate);
    }

    private void notifyUser(User user, TrelloUpdate trelloUpdate) {
        String memberName = trelloUpdate.getAction().getMemberCreator().getFullName();
        Data data = trelloUpdate.getAction().getData();
        List<MessageEntity> entities = new ArrayList<>();
        StringBuilder text = new StringBuilder();

        appendHeader(text, entities);
        appendUserInfo(text, entities, memberName);
        appendAction(text, entities, trelloUpdate.getAction().getDisplay().getTranslationKey(), data);

        bot.sendMessage(text.toString(), user.getChatId(), entities);
    }

    private void appendHeader(StringBuilder text, List<MessageEntity> entities) {
        text.append("Обновление в Trello:\n");
        entities.add(bold("Обновление в Trello:", 0));
    }

    private void appendUserInfo(StringBuilder text, List<MessageEntity> entities, String memberName) {
        int offset = text.length();
        text.append("Пользователь: ").append(memberName).append("\n");
        entities.add(bold("Пользователь:", offset));
    }

    private void appendAction(StringBuilder text, List<MessageEntity> entities, TranslationKey key, Data data) {
        int offset = text.length();
        text.append("Действие: ");
        entities.add(bold("Действие:", offset));
        offset += "Действие: ".length();

        switch (key) {
            case ACTION_COMMENT_ON_CARD -> appendCommentOnCard(text, entities, data, offset);
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST -> appendMoveCardFromListToList(text, entities, data, offset);
        }
    }

    private void appendCommentOnCard(StringBuilder text, List<MessageEntity> entities, Data data, int offset) {
        String cardName = data.getCard().getName();
        String cardUrl = "https://trello.com/c/" + data.getCard().getShortLink();
        String comment = data.getText();

        text.append("прокомментировал карточку ").append(cardName).append("\n");
        entities.add(textLink(cardName, cardUrl, offset + "прокомментировал карточку ".length()));

        int messageOffset = text.length();
        text.append("Сообщением: ").append(comment);
        entities.add(bold("Сообщением:", messageOffset));
    }

    private void appendMoveCardFromListToList(StringBuilder text, List<MessageEntity> entities, Data data, int offset) {
        String listBefore = data.getListBefore().getName();
        String listAfter = data.getListAfter().getName();

        text.append("переместил карточку из \"").append(listBefore).append("\" в \"").append(listAfter).append("\"");
        entities.add(bold(listBefore, offset + "переместил карточку из \"".length()));
        entities.add(bold(listAfter, offset + "переместил карточку из \"".length() + listBefore.length() + "\" в \"".length()));
    }
}
