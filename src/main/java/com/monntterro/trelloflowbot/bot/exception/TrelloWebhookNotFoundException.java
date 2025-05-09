package com.monntterro.trelloflowbot.bot.exception;

public class TrelloWebhookNotFoundException extends RuntimeException {
    public TrelloWebhookNotFoundException(String message) {
        super(message);
    }
}
