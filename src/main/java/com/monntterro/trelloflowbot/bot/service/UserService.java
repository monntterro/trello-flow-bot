package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }
}
