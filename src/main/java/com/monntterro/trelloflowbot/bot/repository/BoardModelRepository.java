package com.monntterro.trelloflowbot.bot.repository;

import com.monntterro.trelloflowbot.bot.entity.BoardModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardModelRepository extends JpaRepository<BoardModel, Long> {

    List<BoardModel> findByUser(User user);
}