package com.monntterro.trelloflowbot.core.controller;

import com.monntterro.trelloflowbot.core.handler.TrelloUpdateHandler;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Trello webhook callbacks.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TrelloUpdateController {
    private final TrelloUpdateHandler trelloUpdateHandler;

    /**
     * HEAD validation endpoint required by Trello webhook API.
     *
     * @param id Webhook identifier
     * @return 200 OK response to confirm webhook URL is valid
     */
    @GetMapping("/{id}")
    public ResponseEntity<Void> validateWebhook(@PathVariable String id) {
        log.debug("Received webhook validation request for ID: {}", id);
        return ResponseEntity.ok().build();
    }

    /**
     * Handler for Trello webhook event callbacks.
     *
     * @param webhookId    Webhook identifier
     * @param trelloUpdate Trello event data
     * @return 200 OK response
     */
    @PostMapping("/{id}")
    public ResponseEntity<Void> handleUpdate(@PathVariable("id") String webhookId,
                                             @RequestBody TrelloUpdate trelloUpdate) {
        log.debug("Received webhook update for ID: {}", webhookId);
        trelloUpdateHandler.handle(trelloUpdate, webhookId);
        return ResponseEntity.ok().build();
    }
}
