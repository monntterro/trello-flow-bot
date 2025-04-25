package com.monntterro.trelloflowbot.core.controller;

import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import com.monntterro.trelloflowbot.core.service.TrelloUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trello-updates")
public class TrelloUpdateController {
    private final TrelloUpdateService service;

    @GetMapping
    public ResponseEntity<?> get() {
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> handleTrelloUpdates(@RequestBody TrelloUpdate trelloUpdate) {
        service.process(trelloUpdate);
        return ResponseEntity.ok().build();
    }
}
