package com.monntterro.trelloflowbot.bot.integration.consumer;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.core.client.TrelloClient;
import com.monntterro.trelloflowbot.core.model.Card;
import com.monntterro.trelloflowbot.core.model.TranslationKey;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.BiPredicate;

@Service
public class NotificationFilter {
    private final TrelloClient trelloClient;
    private final List<BiPredicate<TrelloUpdate, User>> filters;

    public NotificationFilter(TrelloClient trelloClient) {
        this.trelloClient = trelloClient;
        filters = List.of(
                filterByKnownType(),
                filterByCardMember()
        );
    }

    public boolean shouldNotify(TrelloUpdate trelloUpdate, User user) {
        return filters.stream()
                .allMatch(filter -> filter.test(trelloUpdate, user));
    }

    private BiPredicate<TrelloUpdate, User> filterByKnownType() {
        return (update, user) -> update.getAction().getDisplay().getTranslationKey() != TranslationKey.UNKNOWN;
    }

    private BiPredicate<TrelloUpdate, User> filterByCardMember() {
        return (update, user) -> {
            String cardId = update.getAction().getData().getCard().getId();
            Card card = trelloClient.getCard(cardId, user.getToken(), user.getTokenSecret());
            return card.getIdMembers().contains(user.getTrelloMemberId());
        };
    }
}
