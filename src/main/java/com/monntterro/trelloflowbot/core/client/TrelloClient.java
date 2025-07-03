package com.monntterro.trelloflowbot.core.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.monntterro.trelloflowbot.core.exception.AuthenticationException;
import com.monntterro.trelloflowbot.core.model.*;
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

    @Value("${trello.webhook.path}")
    private String webhookPath;

    public Webhook createWebhook(String modelId, String webhookPath, String accessToken, String accessSecret) {
        String callbackUrl = buildCallbackUrl(webhookPath);
        OAuthRequest createWebhookRequest = new OAuthRequest(Verb.POST, "https://api.trello.com/1/webhooks");
        createWebhookRequest.addParameter("callbackURL", callbackUrl);
        createWebhookRequest.addParameter("idModel", modelId);
        String json = executeSignedRequest(createWebhookRequest, accessToken, accessSecret);

        return convert(json, Webhook.class);
    }

    public void deleteWebhook(String webhookId, String accessToken, String accessSecret) {
        OAuthRequest deleteWebhookRequest = new OAuthRequest(Verb.DELETE, "https://api.trello.com/1/webhooks/" + webhookId);
        executeSignedRequest(deleteWebhookRequest, accessToken, accessSecret);
    }

    public void deleteToken(String token, String tokenSecret) {
        OAuthRequest deleteTokenRequest = new OAuthRequest(Verb.DELETE, "https://trello.com/1/tokens/" + token);
        executeSignedRequest(deleteTokenRequest, token, tokenSecret);
    }

    public Member getMe(String accessToken, String accessSecret) {
        OAuthRequest getMeRequest = new OAuthRequest(Verb.GET, "https://api.trello.com/1/members/me");
        String json = executeSignedRequest(getMeRequest, accessToken, accessSecret);
        return convert(json, Member.class);
    }

    public Card getCard(String cardId, String accessToken, String accessSecret) {
        OAuthRequest getCardRequest = new OAuthRequest(Verb.GET, "https://api.trello.com/1/cards/" + cardId);
        String json = executeSignedRequest(getCardRequest, accessToken, accessSecret);

        return convert(json, Card.class);
    }

    public List<com.monntterro.trelloflowbot.core.model.List> getListsForBoard(String boardId, String token,
                                                                               String tokenSecret) {
        OAuthRequest getListsRequest = new OAuthRequest(Verb.GET, "https://api.trello.com/1/boards/" + boardId + "/lists");
        String json = executeSignedRequest(getListsRequest, token, tokenSecret);

        return convert(json, new TypeReference<List<com.monntterro.trelloflowbot.core.model.List>>() {});
    }

    public List<Organization> getUserOrganizations(String token, String tokenSecret) {
        OAuthRequest getOrganizationsRequest = new OAuthRequest(Verb.GET, "https://api.trello.com/1/members/me/organizations");
        String json = executeSignedRequest(getOrganizationsRequest, token, tokenSecret);

        return convert(json, new TypeReference<List<Organization>>() {});
    }

    public List<Board> getBoardsByOrganization(String organizationId, String token, String tokenSecret) {
        OAuthRequest getBoardsRequest = new OAuthRequest(Verb.GET, "https://api.trello.com/1/organizations/" + organizationId + "/boards");
        String json = executeSignedRequest(getBoardsRequest, token, tokenSecret);

        return convert(json, new TypeReference<List<Board>>() {});
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

    private String buildCallbackUrl(String path) {
        return appUrl + webhookPath + "/" + path;
    }
}

