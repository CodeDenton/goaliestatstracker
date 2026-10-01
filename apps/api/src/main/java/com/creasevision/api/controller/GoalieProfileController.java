package com.creasevision.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creasevision.api.service.GoalieProfileService;

@RestController
@RequestMapping("/api/goalies/{goalieId}/profile")
public class GoalieProfileController {
    private final GoalieProfileService profiles;

    public GoalieProfileController(GoalieProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public GoalieProfileService.Profile profile(@PathVariable Long goalieId) {
        return profiles.profile(goalieId);
    }
}
