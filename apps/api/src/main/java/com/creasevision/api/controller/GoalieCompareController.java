package com.creasevision.api.controller;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.creasevision.api.service.GoalieCompareService;
@RestController @RequestMapping("/api/goalies/compare") public class GoalieCompareController { private final GoalieCompareService compare; public GoalieCompareController(GoalieCompareService compare){this.compare=compare;} @GetMapping public GoalieCompareService.Result compare(@RequestParam String season,@RequestParam(name="goalieId") List<Long> goalieIds){return compare.compare(season,goalieIds);} }
