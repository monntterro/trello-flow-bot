package com.monntterro.trelloflowbot.bot.entity;

import com.monntterro.trelloflowbot.bot.converter.TrelloUpdateConverter;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trello_update", columnDefinition = "TEXT")
    @Convert(converter = TrelloUpdateConverter.class)
    private TrelloUpdate trelloUpdate;

    @Column(name = "user_chat_id")
    private Long userChatId;

    @Column(name = "next_reminder_time")
    public Instant nextReminderTime;

    @Column(name = "seen")
    private boolean seen;
}
