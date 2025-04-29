package com.monntterro.trelloflowbot.core.handler;

import com.monntterro.trelloflowbot.bot.service.TrelloUpdateConsumer;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloUpdateHandler {
    private final TrelloUpdateConsumer trelloUpdateConsumer;

    public void handle(TrelloUpdate trelloUpdate, String webhookId) {
        trelloUpdateConsumer.consume(trelloUpdate, webhookId);
    }
}
