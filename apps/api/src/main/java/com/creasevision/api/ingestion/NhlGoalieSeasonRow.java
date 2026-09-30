package com.creasevision.api.ingestion;

/** A single NHL regular-season goalie/team statistical line. */
public record NhlGoalieSeasonRow(
        Long goalieId,
        String goalieFirstName,
        String goalieLastName,
        String teamAbbreviation,
        String teamName,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        Integer overtimeLosses,
        Integer saves,
        Integer shotsAgainst,
        Double goalsAgainstAvg,
        Double savePctg) {
}
