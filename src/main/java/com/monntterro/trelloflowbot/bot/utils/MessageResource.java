package com.monntterro.trelloflowbot.bot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MessageResource {
    private final MessageSource messageSource;

    public String getMessage(String code, String language, Object... args) {
        if (args.length == 0) {
            return messageSource.getMessage(code, null, toLocale(language));
        }
        return messageSource.getMessage(code, args, toLocale(language));
    }

    private Locale toLocale(String language) {
        if (language == null || language.isEmpty()) {
            return Locale.ROOT;
        }
        return Locale.forLanguageTag(language);
    }
}
