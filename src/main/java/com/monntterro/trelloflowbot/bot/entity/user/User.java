package com.monntterro.trelloflowbot.bot.entity.user;


import com.monntterro.trelloflowbot.bot.converter.EncryptedStringConverter;
import com.monntterro.trelloflowbot.bot.entity.BoardModel;
import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id")
    private long telegramId;

    @Column(name = "chat_id")
    private long chatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "token")
    private String token;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "token_secret")
    private String tokenSecret;

    @Column(name = "trello_member_id")
    private String trelloMemberId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BoardModel> boardModels = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TrelloWebhook> trelloWebhooks = new ArrayList<>();
}
