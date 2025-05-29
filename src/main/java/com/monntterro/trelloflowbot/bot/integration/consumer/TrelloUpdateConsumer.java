package com.monntterro.trelloflowbot.bot.integration.consumer;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import com.monntterro.trelloflowbot.core.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloUpdateConsumer implements UpdateConsumer {
    private final NotificationSender notificationSender;
    private final NotificationFilter notificationFilter;
    private final UserService userService;

    @Override
    public void consume(TrelloUpdate trelloUpdate, String webhookId) {
        User user = getUserFromWebhookId(webhookId);
        if (notificationFilter.shouldNotify(trelloUpdate, user)) {
            notificationSender.send(trelloUpdate, user);
        }
    }

    private User getUserFromWebhookId(String webhookId) {
        return userService.findById(Long.parseLong(webhookId))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}