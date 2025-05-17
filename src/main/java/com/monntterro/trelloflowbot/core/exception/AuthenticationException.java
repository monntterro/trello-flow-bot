package com.monntterro.trelloflowbot.core.exception;

public class AuthenticationException extends RuntimeException {
    public static final String message = "Authentication failed. Invalid credentials.";

    public AuthenticationException() {
        super(message);
    }
}
