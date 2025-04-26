package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public boolean existsByChatId(Long chatId) {
        return userRepository.existsByChatId(chatId);
    }

    public boolean existsByTelegramId(Long telegramId) {
        return userRepository.existsByTelegramId(telegramId);
    }

    public void updateChatId(long telegramId, long chatId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new UserNotFoundException("User with telegramId %d not found".formatted(telegramId)));
        user.setChatId(chatId);
        userRepository.save(user);
    }
}
