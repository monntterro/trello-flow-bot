package com.monntterro.trelloflowbot.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthSecret {
    private String token;
    private String tokenSecret;
}
