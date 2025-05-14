package com.monntterro.trelloflowbot.core.service;

import com.monntterro.trelloflowbot.core.model.TrelloUpdate;

public interface UpdateConsumer {

    void consume(TrelloUpdate trelloUpdate, String webhookId);
}
