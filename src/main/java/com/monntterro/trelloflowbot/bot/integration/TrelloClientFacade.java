package com.monntterro.trelloflowbot.bot.integration;

import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.Type;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.TrelloModelNotFoundException;
import com.monntterro.trelloflowbot.bot.exception.TrelloWebhookNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.bot.service.TrelloModelService;
import com.monntterro.trelloflowbot.core.client.TrelloClient;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrelloClientFacade {
    private final TrelloModelService trelloModelService;
    private final TrelloWebhookRepository trelloWebhookRepository;
    private final TrelloClient trelloClient;

    public boolean subscribeToModel(String modelId, String webhookPath, User user) throws AuthenticationException {
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new TrelloModelNotFoundException("Model not found with ID: " + modelId));
        if (trelloModel.isSubscribed()) {
            return false;
        }

        Webhook webhook = trelloClient.createWebhook(modelId, webhookPath, user.getToken(), user.getTokenSecret());
        saveSubscription(trelloModel, webhook, user);

        return true;
    }

    public boolean unsubscribeFromModel(String modelId, User user) throws AuthenticationException {
        TrelloModel trelloModel = trelloModelService.findByModelIdAndUser(modelId, user)
                .orElseThrow(() -> new TrelloModelNotFoundException("Model not found with ID: " + modelId));
        if (!trelloModel.isSubscribed()) {
            return false;
        }

        TrelloWebhook trelloWebhook = trelloWebhookRepository.findByTrelloModel(trelloModel)
                .orElseThrow(() -> new TrelloWebhookNotFoundException("Webhook not found for model: " + modelId));
        deleteSubscription(trelloModel, trelloWebhook, user);

        return true;
    }

    public void removeUserToken(User user) {
        trelloClient.deleteToken(user.getToken(), user.getTokenSecret());
    }

    private List<TrelloModel> mapToTrelloModels(List<com.monntterro.trelloflowbot.core.model.List> boards, User user) {
        return boards.stream()
                .map(list -> TrelloModel.builder()
                        .type(Type.BOARD)
                        .user(user)
                        .modelId(list.getId())
                        .name(list.getName())
                        .build())
                .collect(Collectors.toList());
    }

    private void saveSubscription(TrelloModel trelloModel, Webhook webhook, User user) {
        trelloModel.setSubscribed(true);
        trelloModelService.save(trelloModel);

        TrelloWebhook trelloWebhook = TrelloWebhook.builder()
                .id(webhook.getId())
                .user(user)
                .trelloModel(trelloModel)
                .build();
        trelloWebhookRepository.save(trelloWebhook);
    }

    private void deleteSubscription(TrelloModel trelloModel, TrelloWebhook trelloWebhook, User user) {
        trelloClient.deleteWebhook(trelloWebhook.getId(), user.getToken(), user.getTokenSecret());
        trelloWebhookRepository.delete(trelloWebhook);

        trelloModel.setSubscribed(false);
        trelloModelService.save(trelloModel);
    }

    public List<TrelloModel> getListsForBoard(String boardId, User user) throws AuthenticationException {
        List<com.monntterro.trelloflowbot.core.model.List> lists = trelloClient.getListsForBoard(boardId, user.getToken(), user.getTokenSecret());

        List<TrelloModel> trelloModels = mapToTrelloModels(lists, user);
        return trelloModelService.saveAllOrUpdate(trelloModels, user);
    }
}