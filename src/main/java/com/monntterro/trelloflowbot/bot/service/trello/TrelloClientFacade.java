package com.monntterro.trelloflowbot.bot.service.trello;

import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.TrelloModel;
import com.monntterro.trelloflowbot.bot.entity.trellomodel.Type;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.TrelloModelNotFoundException;
import com.monntterro.trelloflowbot.bot.exception.TrelloWebhookNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.bot.service.TrelloModelService;
import com.monntterro.trelloflowbot.core.api.TrelloClient;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Board;
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

    public List<TrelloModel> getUserBoards(User user) throws AuthenticationException {
        List<Board> boards = trelloClient.getMyBoards(user.getToken(), user.getTokenSecret());
        List<TrelloModel> trelloModels = mapToTrelloModels(boards, user);

        return trelloModelService.saveAllOrUpdate(trelloModels, user);
    }

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

    private List<TrelloModel> mapToTrelloModels(List<Board> boards, User user) {
        return boards.stream()
                .map(board -> TrelloModel.builder()
                        .type(Type.BOARD)
                        .url(board.getShortUrl())
                        .user(user)
                        .modelId(board.getId())
                        .name(board.getName())
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

    public void removeUserToken(User user) {
        trelloClient.deleteToken(user.getToken(), user.getTokenSecret());
    }
}