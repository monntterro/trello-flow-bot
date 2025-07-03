package com.monntterro.trelloflowbot.bot.repository;

import com.monntterro.trelloflowbot.bot.entity.ListModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListModelRepository extends JpaRepository<ListModel, Long> {
}