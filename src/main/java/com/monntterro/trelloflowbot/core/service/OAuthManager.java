package com.monntterro.trelloflowbot.core.service;

import com.github.scribejava.core.model.OAuth1AccessToken;

public interface OAuthManager {
    void successAuth(OAuth1AccessToken accessToken, String token);

    void failureAuth(String token);
}
