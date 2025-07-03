package com.monntterro.trelloflowbot.core.service;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.ParameterList;
import com.github.scribejava.core.oauth.OAuth10aService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final OAuth10aService oAuth10aService;

    @Value("${trello.api.auth_token.expiration}")
    private String tokenExpiration;

    public OAuth1AccessToken getAccessToken(OAuth1RequestToken requestToken, String verifier)
            throws IOException, ExecutionException, InterruptedException {
        return oAuth10aService.getAccessToken(requestToken, verifier);
    }

    public OAuth1RequestToken getRequestToken() throws IOException, ExecutionException, InterruptedException {
        return oAuth10aService.getRequestToken();
    }

    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        ParameterList parameterList = new ParameterList();
        parameterList.add("name", "Trello Flow Bot");
        parameterList.add("scope", "read");
        parameterList.add("expiration", tokenExpiration);

        String url = oAuth10aService.getAuthorizationUrl(requestToken);
        return parameterList.appendTo(url);
    }
}
