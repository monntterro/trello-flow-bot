package com.monntterro.trelloflowbot.bot.repository;

import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrelloModelRepository extends JpaRepository<TrelloModel, Long> {
    Optional<TrelloModel> findByModelIdAndUser(String modelId, User user);
}