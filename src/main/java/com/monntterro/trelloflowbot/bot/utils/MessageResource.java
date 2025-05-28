package com.monntterro.trelloflowbot.bot.utils;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MessageResource {
    private final ResourceBundleMessageSource messageSource;

    public MessageResource() {
        this.messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
    }

    public String getMessage(String code, Object... args) {
        if (args.length == 0) {
            return messageSource.getMessage(code, null, Locale.ROOT);
        }
        return messageSource.getMessage(code, args, Locale.ROOT);
    }
}
