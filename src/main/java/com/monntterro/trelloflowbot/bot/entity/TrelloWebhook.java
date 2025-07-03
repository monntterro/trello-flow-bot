package com.monntterro.trelloflowbot.bot.entity;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trello_webhooks")
public class TrelloWebhook {

    @Id
    @Column(name = "id")
    private String id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_model_id")
    private BoardModel boardModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
