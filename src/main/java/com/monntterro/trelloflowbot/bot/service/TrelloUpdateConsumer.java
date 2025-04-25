package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.core.model.Data;
import com.monntterro.trelloflowbot.core.model.TranslationKey;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloUpdateConsumer {
    private final TelegramBot bot;
    private final UserService userService;

    public void consume(TrelloUpdate trelloUpdate) {
        if (trelloUpdate.getAction().getDisplay().getTranslationKey() == TranslationKey.UNKNOWN) {
            return;
        }

        notifyAllUsers(trelloUpdate);
    }

    private void notifyAllUsers(TrelloUpdate trelloUpdate) {
        userService.findAll().forEach(user -> {
            String text = generateText(trelloUpdate);
            bot.sendMessageWithMarkdown(text, user.getChatId());
        });
    }

    private String generateText(TrelloUpdate trelloUpdate) {
        StringBuilder text = new StringBuilder();

        text.append("*Обновление в Trello:\n*");
        text.append("*Пользователь:* %s\n".formatted(trelloUpdate.getAction().getMemberCreator().getFullName()));
        text.append("*Действие:* ");
        Data data = trelloUpdate.getAction().getData();
        switch (trelloUpdate.getAction().getDisplay().getTranslationKey()) {
            case ACTION_COMMENT_ON_CARD -> {
                text.append("прокомментировал карточку [%s](https://trello.com/c/%s)\n*Сообщением:* %s".formatted(
                        data.getCard().getName(),
                        data.getCard().getShortLink(),
                        data.getText()
                ));
            }
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST -> {
                text.append("переместил карточку из *\"%s\"* в *\"%s\"*".formatted(
                        data.getListBefore().getName(),
                        data.getListAfter().getName()
                ));
            }
        }

        return text.toString();
    }
}
