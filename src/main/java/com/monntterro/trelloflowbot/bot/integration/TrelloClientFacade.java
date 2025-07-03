package com.monntterro.trelloflowbot.bot.integration;

import com.monntterro.trelloflowbot.bot.entity.BoardModel;
import com.monntterro.trelloflowbot.bot.entity.ListModel;
import com.monntterro.trelloflowbot.bot.entity.TrelloWebhook;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.bot.service.BoardModelService;
import com.monntterro.trelloflowbot.bot.service.ListModelService;
import com.monntterro.trelloflowbot.core.client.TrelloClient;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Board;
import com.monntterro.trelloflowbot.core.model.Organization;
import com.monntterro.trelloflowbot.core.model.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloClientFacade {
    private final BoardModelService boardModelService;
    private final ListModelService listModelService;
    private final TrelloWebhookRepository trelloWebhookRepository;
    private final TrelloClient trelloClient;

    public void subscribeToList(Long listId, User user) throws AuthenticationException {
        ListModel listModel = listModelService.findById(listId).orElseThrow();
        listModel.setSubscribed(true);
        listModelService.save(listModel);

        BoardModel boardModel = listModel.getBoardModel();
        if (!boardModel.isSubscribed()) {
            boardModel.setSubscribed(true);
            boardModelService.save(boardModel);

            String webhookPath = user.getId().toString();
            Webhook webhook = trelloClient.createWebhook(boardModel.getModelId(), webhookPath, user.getToken(), user.getTokenSecret());

            TrelloWebhook trelloWebhook = TrelloWebhook.builder()
                    .id(webhook.getId())
                    .user(user)
                    .boardModel(boardModel)
                    .build();
            trelloWebhookRepository.save(trelloWebhook);
        }
    }

    public void unsubscribeFromList(Long listId, User user) throws AuthenticationException {
        ListModel listModel = listModelService.findById(listId).orElseThrow();
        listModel.setSubscribed(false);
        listModelService.save(listModel);

        BoardModel boardModel = listModel.getBoardModel();
        if (boardModel.getListModels().stream().noneMatch(ListModel::isSubscribed)) {
            boardModel.setSubscribed(false);
            boardModelService.save(boardModel);

            TrelloWebhook trelloWebhook = trelloWebhookRepository.findByBoardModel(boardModel).orElseThrow();
            trelloWebhookRepository.delete(trelloWebhook);
            trelloClient.deleteWebhook(trelloWebhook.getId(), user.getToken(), user.getTokenSecret());
        }
    }

    public void removeUserToken(User user) {
        trelloClient.deleteToken(user.getToken(), user.getTokenSecret());
    }

    public List<ListModel> getListsForBoard(Long boardId, User user) throws AuthenticationException {
        BoardModel boardModel = boardModelService.findById(boardId).orElseThrow();
        List<com.monntterro.trelloflowbot.core.model.List> lists = trelloClient.getListsForBoard(boardModel.getModelId(), user.getToken(), user.getTokenSecret());
        List<ListModel> listModels = lists.stream()
                .map(list -> ListModel.builder()
                        .modelId(list.getId())
                        .boardModel(boardModel)
                        .name(list.getName())
                        .build())
                .toList();
        return listModelService.saveAllOrUpdate(listModels, boardModel);
    }

    public List<Organization> getMyOrganizations(User user) {
        return trelloClient.getUserOrganizations(user.getToken(), user.getTokenSecret());
    }

    public List<BoardModel> getBoardsByOrganization(String organizationId, User user) {
        List<Board> boards = trelloClient.getBoardsByOrganization(organizationId, user.getToken(), user.getTokenSecret());
        List<BoardModel> boardModels = boards.stream()
                .map(board -> BoardModel.builder()
                        .modelId(board.getId())
                        .url(board.getShortUrl())
                        .user(user)
                        .name(board.getName())
                        .build())
                .toList();
        return boardModelService.saveAllOrUpdate(boardModels, user);
    }
}