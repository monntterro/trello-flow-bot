package com.monntterro.trelloflowbot.core.controller;

import com.monntterro.trelloflowbot.core.handler.TrelloUpdateHandler;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> validateWebhook(@PathVariable("id") String id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> handleUpdate(@PathVariable("id") String webhookId,
                                             @RequestBody TrelloUpdate trelloUpdate) {
        trelloUpdateHandler.handle(trelloUpdate, webhookId);
        return ResponseEntity.ok().build();
    }
}
