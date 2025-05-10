package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.TrelloModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public void save(TrelloModel trelloModel) {
        trelloModelRepository.save(trelloModel);
    }

    @Transactional
    public List<TrelloModel> saveAllOrUpdate(List<TrelloModel> trelloModels, User user) {
        List<TrelloModel> existingTrelloModels = userService.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found"))
                .getTrelloModels();

        List<TrelloModel> updatedTrelloModels = new ArrayList<>();
        for (TrelloModel trelloModel : trelloModels) {
            Optional<TrelloModel> existingModel = existingTrelloModels.stream()
                    .filter(model -> model.getModelId().equals(trelloModel.getModelId()))
                    .findFirst();

            if (existingModel.isPresent()) {
                TrelloModel modelToUpdate = existingModel.get();
                modelToUpdate.setName(trelloModel.getName());
                modelToUpdate.setUrl(trelloModel.getUrl());
                updatedTrelloModels.add(modelToUpdate);
            } else {
                updatedTrelloModels.add(trelloModel);
            }
        }
        return trelloModelRepository.saveAll(updatedTrelloModels);
    }
}