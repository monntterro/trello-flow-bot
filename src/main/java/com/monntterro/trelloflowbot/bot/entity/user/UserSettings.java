package com.monntterro.trelloflowbot.bot.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(name = "utc_offset")
    private Integer utcOffset;

    @OneToOne(mappedBy = "userSettings", orphanRemoval = true)
    private User user;
}
