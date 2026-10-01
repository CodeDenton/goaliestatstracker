package com.creasevision.api.controller;
import org.springframework.web.bind.annotation.*;
import com.creasevision.api.service.GameIndexService;
import com.creasevision.api.service.GameDetailService;
@RestController @RequestMapping("/api/games")
public class GameController {
    private final GameIndexService index; private final GameDetailService details;
    public GameController(GameIndexService index,GameDetailService details){this.index=index;this.details=details;}
    @GetMapping public GameIndexService.Page games(@RequestParam String season,@RequestParam(required=false) String team,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){return index.games(season,team,page,size);}
    @GetMapping("/{gameId}") public GameDetailService.Detail game(@PathVariable Long gameId){return details.game(gameId);}
}
