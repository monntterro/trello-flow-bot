package com.monntterro.trelloflowbot.bot.integration;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.monntterro.trelloflowbot.bot.entity.user.User;
import com.monntterro.trelloflowbot.bot.exception.UserNotFoundException;
import com.monntterro.trelloflowbot.bot.service.TelegramBot;
import com.monntterro.trelloflowbot.bot.service.UserService;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.core.client.TrelloClient;
import com.monntterro.trelloflowbot.core.model.Member;
import com.monntterro.trelloflowbot.core.service.OAuthManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloOAuthManager implements OAuthManager {
    private final TrelloOAuthSecretStorage secretStorage;
    private final UserService userService;
    private final TelegramBot bot;
    private final MessageResource messageResource;
    private final TrelloClient trelloClient;

    @Override
    public void successAuth(OAuth1AccessToken accessToken, String token) {
        long userTelegramId = secretStorage.getKeyByToken(token);

        Member me = trelloClient.getMe(accessToken.getToken(), accessToken.getTokenSecret());
        User user = userService.findByTelegramId(userTelegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setTrelloMemberId(me.getId());
        user.setToken(accessToken.getToken());
        user.setTokenSecret(accessToken.getTokenSecret());
        userService.save(user);

        String text = messageResource.getMessage("account.login.success");
        bot.sendMessage(text, user.getChatId());

        secretStorage.removeByToken(token);
    }

    @Override
    public void failureAuth(String token) {
        long userTelegramId = secretStorage.getKeyByToken(token);
        User user = userService.findByTelegramId(userTelegramId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String text = messageResource.getMessage("account.login.failure");
        bot.sendMessage(text, user.getChatId());

        secretStorage.removeByToken(token);
    }
}
