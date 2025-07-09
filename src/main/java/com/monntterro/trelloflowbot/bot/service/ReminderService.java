package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.repository.ReminderRepository;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;

    public void create(TrelloUpdate trelloUpdate, Long userChatId) {
        Reminder reminder = Reminder.builder()
                .trelloUpdate(trelloUpdate)
                .userChatId(userChatId)
                .processed(false)
                .nextReminderTime(Instant.now())
                .build();
        reminderRepository.save(reminder);
    }

    public List<Reminder> findAllReadyToSend() {
        return reminderRepository.findAllByProcessedAndNextReminderTimeBefore(false, Instant.now());
    }

    public void saveAll(List<Reminder> reminders) {
        reminderRepository.saveAll(reminders);
    }
}
