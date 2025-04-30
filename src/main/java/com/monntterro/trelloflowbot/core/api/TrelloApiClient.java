package com.monntterro.trelloflowbot.core.api;

import com.monntterro.trelloflowbot.core.model.Board;
import com.monntterro.trelloflowbot.core.model.Webhook;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "trelloApiClient", url = "https://api.trello.com/1")
public interface TrelloApiClient {

    @GetMapping("/members/me/boards")
    List<Board> getMyBoards(@RequestParam("key") String key,
                            @RequestParam("token") String token);

    @PostMapping("/webhooks/")
    Webhook createWebhook(@RequestParam("callbackURL") String callbackURL,
                          @RequestParam("idModel") String idModel,
                          @RequestParam("key") String key,
                          @RequestParam("token") String token);

    @DeleteMapping("/webhooks/{id}")
    void deleteWebhook(@PathVariable("id") String id,
                       @RequestParam("key") String key,
                       @RequestParam("token") String token);
}
