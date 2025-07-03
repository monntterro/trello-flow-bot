package com.monntterro.trelloflowbot.bot.integration;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.BoardModelRepository;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.core.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class TrelloAccountService {
    private final TrelloOAuthSecretStorage oAuthSecretStorage;
    private final OAuthService oAuthService;
    private final UserService userService;
    private final TrelloClientFacade trelloClientFacade;
    private final BoardModelRepository boardModelRepository;
    private final TrelloWebhookRepository trelloWebhookRepository;

    public String getLoginUrl(long userTelegramId) throws IOException, ExecutionException, InterruptedException {
        OAuth1RequestToken requestToken = oAuthService.getRequestToken();
        oAuthSecretStorage.put(requestToken.getToken(), requestToken.getTokenSecret(), userTelegramId);
        return oAuthService.getAuthorizationUrl(requestToken);
    }

    @Transactional
    public void removeAccount(long userTelegramId) {
        User user = userService.findByTelegramId(userTelegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getToken() == null || user.getTokenSecret() == null) {
            return;
        }

        oAuthSecretStorage.removeByToken(userTelegramId);
        trelloClientFacade.removeUserToken(user);

        clearUser(user);
    }

    protected void clearUser(User user) {
        user.setTrelloMemberId(null);
        user.setToken(null);
        user.setTokenSecret(null);

        trelloWebhookRepository.deleteAll(user.getTrelloWebhooks());
        boardModelRepository.deleteAll(user.getBoardModels());

        userService.save(user);
    }
}
