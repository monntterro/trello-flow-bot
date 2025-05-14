package com.monntterro.trelloflowbot.core.controller;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.monntterro.trelloflowbot.bot.utils.MessageResource;
import com.monntterro.trelloflowbot.core.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TrelloOAuthController {
    private final MessageResource messageResource;
    private final OAuthService oAuthService;

    @GetMapping("${trello.api.callback}")
    public ResponseEntity<String> callback(@RequestParam("oauth_token") String token,
                                           @RequestParam("oauth_verifier") String verifier) {
        String tokenSecret = oAuthService.getSecretToken(token);
        OAuth1RequestToken requestToken = new OAuth1RequestToken(token, tokenSecret);
        try {
            OAuth1AccessToken accessToken = oAuthService.getAccessToken(requestToken, verifier);
            oAuthService.successAuth(accessToken, token);
        } catch (Exception e) {
            oAuthService.failureAuth(token);
            return ResponseEntity.ok(messageResource.getMessage("web.trello.oauth.failure"));
        }
        return ResponseEntity.ok(messageResource.getMessage("web.trello.oauth.success"));
    }
}
