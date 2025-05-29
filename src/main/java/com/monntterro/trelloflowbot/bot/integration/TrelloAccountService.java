package com.monntterro.trelloflowbot.bot.integration;

import com.github.scribejava.core.model.OAuth1RequestToken;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.repository.TrelloModelRepository;
import com.monntterro.trelloflowbot.bot.repository.TrelloWebhookRepository;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.core.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class TrelloAccountService {
    private final TrelloOAuthSecretStorage oAuthSecretStorage;
    private final OAuthService oAuthService;
    private final UserService userService;
    private final TrelloClientFacade trelloClientFacade;
    private final TrelloModelRepository trelloModelRepository;
    private final TrelloWebhookRepository trelloWebhookRepository;

    public String getLoginUrl(long userTelegramId) throws IOException, ExecutionException, InterruptedException {
        OAuth1RequestToken requestToken = oAuthService.getRequestToken();
        oAuthSecretStorage.put(requestToken.getToken(), requestToken.getTokenSecret(), userTelegramId);
        return oAuthService.getAuthorizationUrl(requestToken);
    }

    public void removeAccount(long userTelegramId) {
        oAuthSecretStorage.removeByToken(userTelegramId);
        User user = userService.findByTelegramId(userTelegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getToken() == null || user.getTokenSecret() == null) {
            return;
        }
        trelloClientFacade.removeUserToken(user);


        user.setTrelloMemberId(null);
        user.setToken(null);
        user.setTokenSecret(null);

        trelloModelRepository.deleteAll(user.getTrelloModels());
        trelloWebhookRepository.deleteAll(user.getTrelloWebhooks());
        user.getTrelloModels().clear();
        user.getTrelloWebhooks().clear();

        userService.save(user);
    }
}
