package com.monntterro.trelloflowbot.bot.entity;

import com.monntterro.trelloflowbot.bot.entity.user.User;
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
@Table(name = "board_models")
public class BoardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "url")
    private String url;

    @Column(name = "subscribed")
    private boolean subscribed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "boardModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListModel> listModels = new ArrayList<>();
}
