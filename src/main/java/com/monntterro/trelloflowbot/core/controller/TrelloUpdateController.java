package com.monntterro.trelloflowbot.core.controller;

import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import com.monntterro.trelloflowbot.core.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrelloUpdateController {
    private final UpdateConsumer updateConsumer;

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
    public ResponseEntity<Void> consumeUpdate(@PathVariable("id") String webhookId,
                                             @RequestBody TrelloUpdate trelloUpdate) {
        updateConsumer.consume(trelloUpdate, webhookId);
        return ResponseEntity.ok().build();
    }
}
