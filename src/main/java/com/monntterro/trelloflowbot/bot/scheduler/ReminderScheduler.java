package com.monntterro.trelloflowbot.bot.scheduler;

import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.integration.consumer.NotificationSender;
import com.monntterro.trelloflowbot.bot.service.ReminderService;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ReminderService reminderService;
    private final NotificationSender notificationSender;
    private final TelegramBot bot;

    @Value("${reminders.remind-after-minutes}")
    private Long remindAfterMinutes;

    @Scheduled(fixedRateString = "${reminders.scheduler.check-and-send-rate-ms}")
    public void checkAndSendReminders() {
        List<Reminder> readyToSendReminders = reminderService.findAllReadyToSend();

        Instant now = Instant.now();
        readyToSendReminders.forEach(reminder -> {
            Optional<Message> optionalMessage = notificationSender.send(reminder);
            deletePrevReminder(reminder);
            optionalMessage.ifPresent(message -> reminder.setPrevReminderMessageId(message.getMessageId()));
            reminder.setNextReminderTime(now.plusSeconds(remindAfterMinutes * 60));
        });

        reminderService.saveAll(readyToSendReminders);
    }

    private void deletePrevReminder(Reminder reminder) {
        Integer prevReminderMessageId = reminder.getPrevReminderMessageId();
        if (prevReminderMessageId != null) {
            bot.deleteMessage(reminder.getUserChatId(), prevReminderMessageId);
        }
    }
}
