package com.monntterro.trelloflowbot.bot.entity.user;


import com.monntterro.trelloflowbot.bot.converter.EncryptedStringConverter;
import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
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
    @Column(name = "trello_api_key")
    private String trelloApiKey;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "trello_api_token")
    private String trelloApiToken;

    @Column(name = "language")
    private String language;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TrelloModel> trelloModels = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TrelloWebhook> trelloWebhooks = new ArrayList<>();
}
