package com.monntterro.trelloflowbot.core.controller;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.core.service.OAuthManager;
import com.monntterro.trelloflowbot.core.service.OAuthSecretStorage;
import com.monntterro.trelloflowbot.core.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TrelloOAuthController {
    private final MessageResource messageResource;
    private final OAuthService oAuthService;
    private final OAuthManager oAuthManager;
    private final OAuthSecretStorage oAuthSecretStorage;

    @GetMapping("${trello.api.path}")
    public ResponseEntity<String> callback(@RequestParam("oauth_token") String token,
                                           @RequestParam("oauth_verifier") String verifier) {
        Optional<String> tokenSecretOpt = oAuthSecretStorage.getSecretToken(token);
        if (tokenSecretOpt.isEmpty()) {
            return ResponseEntity.ok(messageResource.getMessage("web.trello.oauth.expired"));
        }

        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecretOpt.get());
        try {
            OAuth1AccessToken accessToken = oAuthService.getAccessToken(requestToken, verifier);
            oAuthManager.successAuth(accessToken, token);
        } catch (Exception e) {
            oAuthManager.failureAuth(token);
            return ResponseEntity.ok(messageResource.getMessage("web.trello.oauth.failure"));
        }
        return ResponseEntity.ok(messageResource.getMessage("web.trello.oauth.success"));
    }
}
