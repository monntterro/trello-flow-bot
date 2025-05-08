package com.monntterro.trelloflowbot.core.handler;

import com.monntterro.trelloflowbot.bot.service.TrelloUpdateConsumer;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for processing Trello webhook updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloUpdateHandler {
    private final TrelloUpdateConsumer trelloUpdateConsumer;

    /**
     * Handles incoming Trello update events.
     *
     * @param trelloUpdate The Trello update payload
     * @param webhookId    ID of the webhook that triggered this update
     */
    public void handle(TrelloUpdate trelloUpdate, String webhookId) {
        log.debug("Processing Trello update for webhook ID: {}", webhookId);
        trelloUpdateConsumer.consume(trelloUpdate, webhookId);
    }
}
