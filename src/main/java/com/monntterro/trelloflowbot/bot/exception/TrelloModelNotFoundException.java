package com.monntterro.trelloflowbot.bot.exception;

public class TrelloModelNotFoundException extends RuntimeException {
    public TrelloModelNotFoundException(String message) {
        super(message);
    }
}
