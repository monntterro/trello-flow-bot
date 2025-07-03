package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.BoardModel;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.repository.BoardModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardModelService {
    private final BoardModelRepository boardModelRepository;

    public void save(BoardModel boardModel) {
        boardModelRepository.save(boardModel);
    }

    @Transactional
    public List<BoardModel> saveAllOrUpdate(List<BoardModel> boardModels, User user) {
        List<BoardModel> existingBoardModels = boardModelRepository.findByUser(user);

        List<BoardModel> updatedBoardModels = new ArrayList<>();
        for (BoardModel boardModel : boardModels) {
            Optional<BoardModel> existingModel = existingBoardModels.stream()
                    .filter(model -> model.getModelId().equals(boardModel.getModelId()))
                    .findFirst();

            if (existingModel.isPresent()) {
                BoardModel modelToUpdate = existingModel.get();
                modelToUpdate.setName(boardModel.getName());
                modelToUpdate.setModelId(boardModel.getModelId());
                modelToUpdate.setUser(user);
                modelToUpdate.setUrl(boardModel.getUrl());
                updatedBoardModels.add(modelToUpdate);
            } else {
                updatedBoardModels.add(boardModel);
            }
        }
        return boardModelRepository.saveAll(updatedBoardModels);
    }

    public Optional<BoardModel> findById(Long id) {
        return boardModelRepository.findById(id);
    }
}