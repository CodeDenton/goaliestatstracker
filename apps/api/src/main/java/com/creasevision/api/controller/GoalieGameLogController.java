package com.creasevision.api.controller;
import org.springframework.web.bind.annotation.*;
import com.creasevision.api.service.GoalieGameLogService;
@RestController @RequestMapping("/api/goalies/{goalieId}/games") public class GoalieGameLogController { private final GoalieGameLogService games; public GoalieGameLogController(GoalieGameLogService games){this.games=games;} @GetMapping public GoalieGameLogService.Page games(@PathVariable Long goalieId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="25") int size){return games.games(goalieId,page,size);} }
