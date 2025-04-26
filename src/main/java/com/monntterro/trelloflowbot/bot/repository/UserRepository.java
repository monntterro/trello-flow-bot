package com.monntterro.trelloflowbot.bot.repository;

import com.monntterro.trelloflowbot.bot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByChatId(Long chatId);

    boolean existsByTelegramId(Long telegramId);

    Optional<User> findByTelegramId(Long telegramId);
}
