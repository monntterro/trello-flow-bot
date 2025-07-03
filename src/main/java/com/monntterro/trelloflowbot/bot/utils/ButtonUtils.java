package com.monntterro.trelloflowbot.bot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.Collection;
import java.util.List;

public class ButtonUtils {

    public static InlineKeyboardButton button(String text, String callback) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callback)
                .build();
    }

    public static InlineKeyboardButton urlButton(String text, String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
    }

    public static InlineKeyboardRow row(InlineKeyboardButton button, InlineKeyboardButton... buttons) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        row.addAll(List.of(buttons));
        return row;
    }

    public static InlineKeyboardRow row(Collection<InlineKeyboardButton> buttons) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(buttons);
        return row;
    }

    public static InlineKeyboardMarkup inlineKeyboard(InlineKeyboardRow row, InlineKeyboardRow... rows) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row)
                .keyboard(List.of(rows))
                .build();
    }

    public static InlineKeyboardMarkup inlineKeyboard(Collection<InlineKeyboardRow> rows) {
        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }
}
