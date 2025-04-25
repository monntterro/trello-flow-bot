package com.monntterro.trelloflowbot.core.service;

import com.monntterro.trelloflowbot.bot.service.TrelloUpdateConsumer;
import com.monntterro.trelloflowbot.core.model.TrelloUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrelloUpdateService {
    private final TrelloUpdateConsumer trelloUpdateConsumer;

    public void process(TrelloUpdate trelloUpdate) {
        trelloUpdateConsumer.consume(trelloUpdate);
    }
}
