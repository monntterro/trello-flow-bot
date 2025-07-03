package com.monntterro.trelloflowbot.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Action {
    private Data data;
    private Type type;
    private MemberCreator memberCreator;
    private Display display;
}
