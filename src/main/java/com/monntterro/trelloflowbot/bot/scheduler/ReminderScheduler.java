package com.monntterro.trelloflowbot.bot.scheduler;

import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.integration.consumer.NotificationSender;
import com.monntterro.trelloflowbot.bot.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final ReminderService reminderService;
    private final NotificationSender notificationSender;

    @Value("${reminders.remind-after-minutes}")
    private Long remindAfterMinutes;

    @Scheduled(fixedRateString = "${reminders.scheduler.check-and-send-rate-ms}")
    public void checkAndSendReminders() {
        List<Reminder> readyToSendReminders = reminderService.findAllReadyToSend();

        Instant now = Instant.now();
        readyToSendReminders.forEach(reminder -> {
            notificationSender.send(reminder);
            reminder.setNextReminderTime(now.plusSeconds(remindAfterMinutes * 60));
        });

        reminderService.saveAll(readyToSendReminders);
    }
}
