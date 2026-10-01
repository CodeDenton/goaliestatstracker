package com.creasevision.api.dto;

import java.util.List;

public record ShotMapDTO(
        Long goalieId,
        String seasonId,
        Long gameId,
        List<ShotDTO> shots,
        List<ZoneDTO> zones) {

    public record ShotDTO(Long gameId, int eventId, int xCoordinate, int yCoordinate, String outcome) {
    }

    public record ZoneDTO(String code, int shots, int saves, int goals, double savePctg) {
    }
}
