package com.monntterro.trelloflowbot.bot.processor;

import com.monntterro.trelloflowbot.bot.constants.ButtonConstants;
import com.monntterro.trelloflowbot.bot.entity.Role;
import com.monntterro.trelloflowbot.bot.entity.State;
import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import static com.monntterro.trelloflowbot.bot.utils.ButtonUtils.*;

@Component
@RequiredArgsConstructor
public class RegistrationProcessor {
    private final UserService userService;
    private final TelegramBot bot;

    public void process(Message message, User user) {
        switch (message.getText()) {
            case ButtonConstants.USER_ROLE -> registerAsUser(message, user);
            default -> authorize(message, user);
        }
    }

    private void registerAsUser(Message message, User user) {
        user.setRole(Role.USER);
        user.setState(State.IDLE);
        userService.save(user);

        String text = "Вы выбрали роль пользователя. Теперь вы сможете получать уведомления.";
        bot.sendMessageAndDeleteReplyMarkup(text, message.getChatId());
    }

    public void authorize(Message message, User user) {
        user.setState(State.CHOOSING_ROLE);
        userService.save(user);

        String text = "Для начала нужно зарегистрироваться, выберите роль.";
        long chatId = message.getChatId();
        ReplyKeyboardMarkup replyMarkup = replyMarkup(replyRow(replyButton(ButtonConstants.USER_ROLE)));
        bot.sendMessageWithReplyMarkup(text, chatId, replyMarkup);
    }
}
