package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.config.props.TelegramBotProperties;
import com.monntterro.trelloflowbot.bot.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final TelegramBotProperties properties;
    private final UpdateHandler updateHandler;

    public TelegramBot(TelegramBotProperties properties, @Lazy UpdateHandler updateHandler) {
        this.properties = properties;
        this.updateHandler = updateHandler;
        this.telegramClient = new OkHttpTelegramClient(properties.getToken());
    }

    @Override
    public void consume(Update update) {
        updateHandler.handle(update);
    }

    @Override
    public String getBotToken() {
        return properties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    public void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .parseMode(ParseMode.MARKDOWNV2)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(String text, long chatId, InlineKeyboardMarkup markup) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void editMessage(String text, long chatId, int messageId, InlineKeyboardMarkup markup) {
        EditMessageText sendMessage = EditMessageText.builder()
                .text(text)
                .chatId(chatId)
                .messageId(messageId)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(markup)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void editMessage(String text, long chatId, int messageId) {
        EditMessageText sendMessage = EditMessageText.builder()
                .text(text)
                .chatId(chatId)
                .messageId(messageId)
                .parseMode(ParseMode.MARKDOWNV2)
                .replyMarkup(null)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
