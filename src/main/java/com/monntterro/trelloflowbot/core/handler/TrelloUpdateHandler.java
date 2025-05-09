package com.monntterro.trelloflowbot.core.handler;

import com.monntterro.trelloflowbot.bot.service.TrelloUpdateConsumer;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloUpdateHandler {
    private final TrelloUpdateConsumer trelloUpdateConsumer;

    public void handle(TrelloUpdate trelloUpdate, String webhookId) {
        trelloUpdateConsumer.consume(trelloUpdate, webhookId);
    }
}
