package com.monntterro.trelloflowbot.bot.repository;

import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrelloWebhookRepository extends JpaRepository<TrelloWebhook, String> {
    Optional<TrelloWebhook> findByTrelloModel(TrelloModel trelloModel);
}