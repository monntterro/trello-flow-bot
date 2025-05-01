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

        long userId = Long.parseLong(webhookId);
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with id %d not found".formatted(userId)));

        notifyUsers(user, trelloUpdate);
    }

    private void notifyUsers(User user, TrelloUpdate trelloUpdate) {
        String memberName = trelloUpdate.getAction().getMemberCreator().getFullName();
        Data data = trelloUpdate.getAction().getData();
        List<MessageEntity> entities = new ArrayList<>();

        StringBuilder text = new StringBuilder("Обновление в Trello:\n");
        int offset = 0;

        entities.add(bold("Обновление в Trello:", offset));
        offset += "Обновление в Trello:".length() + 1;

        text.append("Пользователь: ").append(memberName).append("\n");
        entities.add(bold("Пользователь:", offset));
        offset += "Пользователь: ".length() + memberName.length() + 1;

        text.append("Действие: ");
        entities.add(bold("Действие:", offset));
        offset += "Действие: ".length();

        switch (trelloUpdate.getAction().getDisplay().getTranslationKey()) {
            case ACTION_COMMENT_ON_CARD -> {
                String cardName = data.getCard().getName();
                String cardUrl = "https://trello.com/c/" + data.getCard().getShortLink();
                String comment = data.getText();

                text.append("прокомментировал карточку ")
                        .append(cardName)
                        .append("\nСообщением: ")
                        .append(comment);

                entities.add(textLink(cardName, cardUrl, offset + "прокомментировал карточку ".length()));

                int messageOffset = offset + "прокомментировал карточку ".length() + cardName.length() + 1;
                entities.add(bold("Сообщением:", messageOffset));
            }
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST -> {
                String listBefore = data.getListBefore().getName();
                String listAfter = data.getListAfter().getName();

                text.append("переместил карточку из \"")
                        .append(listBefore)
                        .append("\" в \"")
                        .append(listAfter)
                        .append("\"");

                entities.add(bold(listBefore, offset + "переместил карточку из \"".length()));
                entities.add(bold(listAfter, offset + "переместил карточку из \"".length() +
                                             listBefore.length() + "\" в \"".length()));
            }
        }

        bot.sendMessage(text.toString(), user.getChatId(), entities);
    }
}
