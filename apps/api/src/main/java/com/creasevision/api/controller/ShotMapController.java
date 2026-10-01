package com.creasevision.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.creasevision.api.dto.ShotMapDTO;
import com.creasevision.api.service.ShotMapService;

@RestController
@RequestMapping("/api/goalies/{goalieId}/shot-map")
public class ShotMapController {
    private final ShotMapService shotMaps;

    public ShotMapController(ShotMapService shotMaps) {
        this.shotMaps = shotMaps;
    }

    @GetMapping
    public ShotMapDTO map(
            @PathVariable Long goalieId,
            @RequestParam String season,
            @RequestParam(required = false) Long gameId) {
        return shotMaps.goalieMap(goalieId, season, gameId);
    }
}
