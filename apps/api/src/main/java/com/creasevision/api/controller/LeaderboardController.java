package com.creasevision.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creasevision.api.service.LeaderboardService;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    private final LeaderboardService leaderboard;

    public LeaderboardController(LeaderboardService leaderboard) {
        this.leaderboard = leaderboard;
    }

    @GetMapping
    public LeaderboardService.LeaderboardPage leaderboard(
            @RequestParam String season,
            @RequestParam(required = false) String team,
            @RequestParam(defaultValue = "0") int minGames,
            @RequestParam(defaultValue = "savePctg") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(required = false) String search) {
        return leaderboard.leaderboard(season, team, minGames, sort, direction, page, size, search);
    }
}
