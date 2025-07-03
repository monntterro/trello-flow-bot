package com.monntterro.trelloflowbot.bot.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "list_models")
public class ListModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id")
    private String modelId;

    @Column(name = "name")
    private String name;

    @Column(name = "subscribed")
    private boolean subscribed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_model_id")
    private BoardModel boardModel;
}
