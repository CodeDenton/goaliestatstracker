package com.creasevision.api.ingestion;

import java.time.LocalDate;

public record NhlGoalieGameRow(Long gameId, String seasonId, LocalDate gameDate, short gameType,
        String homeTeam, String awayTeam, Integer homeScore, Integer awayScore, String gameState,
        Long goalieId, String team, Boolean starter, String decision, Integer timeOnIceSeconds,
        Integer shotsAgainst, Integer saves, Integer goalsAgainst, Double savePctg) { }
