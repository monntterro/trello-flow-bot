package com.monntterro.trelloflowbot.bot.service;

import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.Type;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.core.api.TrelloClient;
import com.monntterro.trelloflowbot.core.model.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrelloClientFacade {
    private final TrelloModelService trelloModelService;
    private final TrelloWebhookRepository trelloWebhookRepository;
    private final TrelloClient trelloClient;

    public List<TrelloModel> getUserBoards(User user) {
        List<TrelloModel> newTrelloModels = trelloClient.getMyBoards(user.getTrelloApiKey(), user.getTrelloApiToken())
                .stream()
                .map(board -> TrelloModel.builder()
                        .type(Type.BOARD)
                        .url(board.getShortUrl())
                        .isSubscribed(false)
                        .user(user)
                        .modelId(board.getId())
                        .name(board.getName())
                        .build())
                .toList();
        return trelloModelService.saveAll(newTrelloModels);
    }

    public boolean subscribeToModel(String modelId, String webhookPath, User user) {
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new RuntimeException("Trello model not found"));
        if (trelloModel.isSubscribed()) {
            return false;
        }

        Webhook webhook = trelloClient.subscribeToModel(modelId, webhookPath, user.getTrelloApiKey(), user.getTrelloApiToken());
        trelloModel.setSubscribed(true);
        trelloModelService.save(trelloModel);

        TrelloWebhook trelloWebhook = TrelloWebhook.builder()
                .id(webhook.getId())
                .user(user)
                .trelloModel(trelloModel)
                .build();
        trelloWebhookRepository.save(trelloWebhook);
        return true;
    }

    public boolean unsubscribeFromModel(String modelId, User user) {
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new RuntimeException("Trello model not found"));
        if (!trelloModel.isSubscribed()) {
            return false;
        }
        trelloModel.setSubscribed(false);
        trelloModelService.save(trelloModel);

        TrelloWebhook trelloWebhook = trelloWebhookRepository.findByTrelloModel(trelloModel)
                .orElseThrow(() -> new RuntimeException("Trello webhook not found"));
        trelloWebhookRepository.delete(trelloWebhook);
        trelloClient.unsubscribeFromModel(trelloWebhook.getId(), user.getTrelloApiKey(), user.getTrelloApiToken());
        return true;
    }
}
