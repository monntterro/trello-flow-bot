package com.monntterro.trelloflowbot.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookUpdateController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getOk(@PathVariable String id) {
        return ResponseEntity.ok().build();
    }
}
