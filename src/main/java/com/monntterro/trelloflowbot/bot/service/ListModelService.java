package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.BoardModel;
import com.monntterro.trelloflowbot.bot.entity.ListModel;
import com.monntterro.trelloflowbot.bot.repository.ListModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ListModelService {
    private final ListModelRepository listModelRepository;

    public Optional<ListModel> findById(Long id) {
        return listModelRepository.findById(id);
    }

    public void save(ListModel listModel) {
        listModelRepository.save(listModel);
    }

    @Transactional
    public List<ListModel> saveAllOrUpdate(List<ListModel> listModels, BoardModel boardModel) {
        List<ListModel> existingListModels = boardModel.getListModels();

        List<ListModel> updatedListModels = new ArrayList<>();
        for (ListModel listModel : listModels) {
            Optional<ListModel> existingModel = existingListModels.stream()
                    .filter(model -> model.getModelId().equals(listModel.getModelId()))
                    .findFirst();

            if (existingModel.isPresent()) {
                ListModel modelToUpdate = existingModel.get();
                modelToUpdate.setModelId(listModel.getModelId());
                modelToUpdate.setBoardModel(listModel.getBoardModel());
                modelToUpdate.setName(listModel.getName());
                updatedListModels.add(modelToUpdate);
            } else {
                updatedListModels.add(listModel);
            }
        }
        return listModelRepository.saveAll(updatedListModels);
    }
}
