package com.monntterro.trelloflowbot.bot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageResource {
    private final MessageSource messageSource;

    public String getMessage(String code, Object... args) {
        if (args.length == 0) {
            return messageSource.getMessage(code, null, Locale.ROOT);
        }
        return messageSource.getMessage(code, args, Locale.ROOT);
    }
}
