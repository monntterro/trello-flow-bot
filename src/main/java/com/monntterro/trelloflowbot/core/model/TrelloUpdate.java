package com.monntterro.trelloflowbot.core.model;

import lombok.Data;

@Data
public class TrelloUpdate {
    private Action action;
    private Model model;
}
