package com.monntterro.trelloflowbot.bot.entity;


import com.monntterro.trelloflowbot.bot.converter.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.*;

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
}
