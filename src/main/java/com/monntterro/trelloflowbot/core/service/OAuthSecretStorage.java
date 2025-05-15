package com.monntterro.trelloflowbot.core.service;

import java.util.Optional;

public interface OAuthSecretStorage {

    Optional<String> getSecretToken(String token);
}
