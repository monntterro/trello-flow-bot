package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.config.props.TelegramBotProperties;
import com.monntterro.trelloflowbot.bot.handler.UpdateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.AfterBotRegistration;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TelegramBot extends SpringTelegramWebhookBot {
    private final TelegramClient telegramClient;

    @Autowired
    public TelegramBot(TelegramBotProperties properties, @Lazy UpdateHandler updateHandler,
                       WebhookService webhookService) {
        super(properties.getPath(), updateHandler::handle, webhookService::setWebhook, webhookService::deleteWebhook);
        this.telegramClient = new OkHttpTelegramClient(properties.getToken());
    }

    @AfterBotRegistration
    public void afterBotRegistration() {
        setCommands();
    }

    private void setCommands() {
        List<BotCommand> commands = Arrays.asList(
                new BotCommand("/menu", "Открыть меню"),
                new BotCommand("/login", "Авторизоваться через Trello")
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

    public void sendMessage(String text, long chatId, List<MessageEntity> entities) {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .entities(entities)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
