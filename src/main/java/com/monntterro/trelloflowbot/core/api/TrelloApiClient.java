package com.monntterro.trelloflowbot.core.api;

import com.monntterro.trelloflowbot.core.model.Board;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trelloClient", url = "https://api.trello.com/1")
public interface TrelloApiClient {

    @GetMapping("/members/me/boards")
    List<Board> getBoards(@RequestParam("key") String key, @RequestParam("token") String token);
}
