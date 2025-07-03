package com.monntterro.trelloflowbot.bot.integration.consumer;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.core.client.TrelloClient;
import com.monntterro.trelloflowbot.core.model.Card;
import com.monntterro.trelloflowbot.core.model.TranslationKey;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.BiPredicate;
import java.util.stream.Stream;

@Service
public class NotificationFilter {
    private final TrelloClient trelloClient;

    @Autowired
    public NotificationFilter(TrelloClient trelloClient) {
        this.trelloClient = trelloClient;
    }

    public boolean shouldNotify(TrelloUpdate trelloUpdate, User user) {
        TranslationKey actionType = trelloUpdate.getAction().getDisplay().getTranslationKey();

        if (ownMemberAction(trelloUpdate, user)) {
            return false;
        }

        return switch (actionType) {
            case ACTION_COMMENT_ON_CARD -> commentOnCard(trelloUpdate, user);
            case ACTION_MOVE_CARD_FROM_LIST_TO_LIST -> filterBySubscribedLists().test(trelloUpdate, user);
            case UNKNOWN -> false;
        };
    }

    private boolean commentOnCard(TrelloUpdate trelloUpdate, User user) {
        return Stream.of(filterByUserMention(),
                         filterByCardMember())
                .anyMatch(predicate -> predicate.test(trelloUpdate, user));
    }

    private boolean ownMemberAction(TrelloUpdate trelloUpdate, User user) {
        return trelloUpdate.getAction().getMemberCreator().getId().equals(user.getTrelloMemberId());
    }

    private BiPredicate<TrelloUpdate, User> filterByUserMention() {
        return (update, user) -> {
            String username = trelloClient.getMe(user.getToken(), user.getTokenSecret()).getUsername();
            return update.getAction().getData().getText().contains("@" + username);
        };
    }

    private BiPredicate<TrelloUpdate, User> filterByCardMember() {
        return (update, user) -> {
            String cardId = update.getAction().getData().getCard().getId();
            Card card = trelloClient.getCard(cardId, user.getToken(), user.getTokenSecret());
            return card.getIdMembers().contains(user.getTrelloMemberId());
        };
    }

    private BiPredicate<TrelloUpdate, User> filterBySubscribedLists() {
        return (update, user) -> {
            String listId = update.getAction().getData().getListAfter().getId();
            return user.getBoardModels().stream()
                    .flatMap(boardModel -> boardModel.getListModels().stream())
                    .anyMatch(listModel -> listModel.getModelId().equals(listId) && listModel.isSubscribed());
        };
    }
}
