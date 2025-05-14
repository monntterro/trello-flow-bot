package com.monntterro.trelloflowbot.core.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.Board;
import com.monntterro.trelloflowbot.core.model.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloClient {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OAuth10aService oAuthService;

    @Value("${app.url}")
    private String appUrl;

    public List<Board> getMyBoards(String accessToken, String accessSecret) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.trello.com/1/members/me/boards");
        String json = executeSignedRequest(request, accessToken, accessSecret);

        return convert(json, new TypeReference<List<Board>>() {});
    }

    public Webhook subscribeToModel(String modelId, String webhookPath, String accessToken, String accessSecret) {
        String callbackUrl = buildCallbackUrl(webhookPath);
        OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.trello.com/1/webhooks");
        request.addParameter("callbackURL", callbackUrl);
        request.addParameter("idModel", modelId);
        String json = executeSignedRequest(request, accessToken, accessSecret);

        return convert(json, Webhook.class);
    }

    public void unsubscribeFromModel(String webhookId, String accessToken, String accessSecret) {
        OAuthRequest request = new OAuthRequest(Verb.DELETE, "https://api.trello.com/1/webhooks/" + webhookId);
        executeSignedRequest(request, accessToken, accessSecret);
    }

    private String executeSignedRequest(OAuthRequest request, String accessToken, String accessSecret) {
        OAuth1AccessToken token = new OAuth1AccessToken(accessToken, accessSecret);
        oAuthService.signRequest(token, request);

        try {
            Response response = oAuthService.execute(request);
            if (response.isSuccessful()) {
                return response.getBody();
            }
        } catch (Exception ignored) {
        }
        throw new AuthenticationException();
    }

    private <T> T convert(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T convert(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildCallbackUrl(String webhookPath) {
        return appUrl + "/" + webhookPath;
    }
}

