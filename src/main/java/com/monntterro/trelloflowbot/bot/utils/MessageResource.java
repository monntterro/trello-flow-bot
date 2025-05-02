package com.monntterro.trelloflowbot.bot.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResource {
    private final MessageSource messageSource;

    @Autowired
    public MessageResource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code, String language, Object... args) {
        if (args.length == 0) {
            return messageSource.getMessage(code, null, Locale.forLanguageTag(language));
        }
        return messageSource.getMessage(code, args, Locale.forLanguageTag(language));
    }
}
