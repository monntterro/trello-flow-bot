package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.Reminder;
import com.monntterro.trelloflowbot.bot.repository.ReminderRepository;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final ReminderRepository reminderRepository;

    public void create(TrelloUpdate trelloUpdate, Long userChatId) {
        Reminder reminder = Reminder.builder()
                .trelloUpdate(trelloUpdate)
                .userChatId(userChatId)
                .seen(false)
                .nextReminderTime(Instant.now())
                .build();
        reminderRepository.save(reminder);
    }

    public List<Reminder> findAllReadyToSend() {
        return reminderRepository.findAllBySeenAndNextReminderTimeBefore(false, Instant.now());
    }

    public void saveAll(List<Reminder> reminders) {
        reminderRepository.saveAll(reminders);
    }

    public Optional<Reminder> findById(Long id) {
        return reminderRepository.findById(id);
    }

    public void save(Reminder reminder) {
        reminderRepository.save(reminder);
    }
}
