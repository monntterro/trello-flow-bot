package com.monntterro.trelloflowbot.core.model;

@lombok.Data
public class Action {
    private Data data;
    private Type type;
    private MemberCreator memberCreator;
    private Display display;
}
