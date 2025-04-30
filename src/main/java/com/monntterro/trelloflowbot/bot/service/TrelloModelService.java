package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.Type;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.repository.TrelloModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrelloModelService {
    private final UserService userService;
    private final TrelloModelRepository trelloModelRepository;

    public Optional<TrelloModel> findByModelIdAndUser(String modelId, User user) {
        return trelloModelRepository.findByModelIdAndUser(modelId, user);
    }

    @Transactional
    public List<TrelloModel> findAllByUserAndType(User user, Type modelType) {
        return userService.findById(user.getId()).stream()
                .flatMap(u -> u.getTrelloModels().stream())
                .filter(model -> model.getType() == modelType)
                .sorted(Comparator.comparing(TrelloModel::isSubscribed).reversed())
                .toList();
    }

    public void save(TrelloModel trelloModel) {
        trelloModelRepository.save(trelloModel);
    }

    public List<TrelloModel> saveAll(List<TrelloModel> trelloModels) {
        return trelloModelRepository.saveAll(trelloModels);
    }
}