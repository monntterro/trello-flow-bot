package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Webhook {
    private String id;
    private String idModel;
    private String callbackURL;
}
