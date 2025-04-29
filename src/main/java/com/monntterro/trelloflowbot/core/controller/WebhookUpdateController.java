package com.monntterro.trelloflowbot.core.controller;

import com.monntterro.trelloflowbot.core.handler.TrelloUpdateHandler;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WebhookUpdateController {
    private final TrelloUpdateHandler trelloUpdateHandler;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOk(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> handleUpdate(@PathVariable("id") String webhookId,
                                          @RequestBody TrelloUpdate trelloUpdate) {
        System.out.println(webhookId);
        trelloUpdateHandler.handle(trelloUpdate, webhookId);
        return ResponseEntity.ok().build();
    }
}
