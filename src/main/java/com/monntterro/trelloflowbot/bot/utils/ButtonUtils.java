package com.monntterro.trelloflowbot.bot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ButtonUtils {

    public static KeyboardButton replyButton(String text) {
        return KeyboardButton.builder()
                .text(text)
                .build();
    }

    public static KeyboardRow replyRow(KeyboardButton button, KeyboardButton... buttons) {
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        row.addAll(List.of(buttons));
        return row;
    }

    public static ReplyKeyboardMarkup replyMarkup(KeyboardRow row, KeyboardRow... rows) {
        List<KeyboardRow> keyboardRows = new ArrayList<>(Collections.singletonList(row));
        keyboardRows.addAll(List.of(rows));
        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .isPersistent(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();
    }
}
