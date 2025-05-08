package com.monntterro.trelloflowbot.core.exception;

public class AuthenticationException extends RuntimeException {
    private static final String MESSAGE = "Invalid Trello API key or token";

    public AuthenticationException() {
        super(MESSAGE);
    }
}
