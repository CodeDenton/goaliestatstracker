package com.creasevision.api.ingestion;

public record NhlShotEventRow(
        long gameId,
        String seasonId,
        int eventId,
        long goalieId,
        String defendingTeam,
        int xCoordinate,
        int yCoordinate,
        boolean goal) {
}
