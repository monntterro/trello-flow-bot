package com.monntterro.trelloflowbot.core.model;

import lombok.Data;

@Data
public class Webhook {
    private String id;
    private String idModel;
    private String callbackURL;
}
