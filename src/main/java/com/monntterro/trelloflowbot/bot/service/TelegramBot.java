package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.config.props.TelegramBotProperties;
import com.monntterro.trelloflowbot.bot.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.List;

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

    @AfterBotRegistration
    public void afterBotRegistration() {
        setCommands();
    }

    private void setCommands() {
        List<BotCommand> commands = Arrays.asList(
                new BotCommand("/menu", "Open menu"),
                new BotCommand("/change_token_and_key", "Change trello token and key")
        );

        SetMyCommands setMyCommands = new SetMyCommands(commands);
        try {
            telegramClient.execute(setMyCommands);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
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
                .replyMarkup(null)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void editMessage(String text, long chatId, int messageId, List<MessageEntity> messageEntities,
                            InlineKeyboardMarkup markup) {
        EditMessageText sendMessage = EditMessageText.builder()
                .text(text)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(markup)
                .entities(messageEntities)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessage(String string, long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = SendMessage.builder()
                .text(string)
                .chatId(chatId)
                .entities(entities)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public void sendMessageWithMarkdown(String text, Long chatId) {
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
}
