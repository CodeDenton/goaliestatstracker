package com.creasevision.api.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.creasevision.api.dto.ShotMapDTO;
import com.creasevision.api.model.ShotEvent;
import com.creasevision.api.repository.ShotEventRepository;

@Service
public class ShotMapService {
    private final ShotEventRepository shotEvents;

    public ShotMapService(ShotEventRepository shotEvents) {
        this.shotEvents = shotEvents;
    }

    @Transactional(readOnly = true)
    public ShotMapDTO goalieMap(Long goalieId, String seasonId, Long gameId) {
        List<ShotEvent> events = gameId == null
                ? shotEvents.findByGoalieIdAndSeasonIdOrderByGameIdAscNhlEventIdAsc(goalieId, seasonId)
                : shotEvents.findByGoalieIdAndGameIdOrderByNhlEventIdAsc(goalieId, gameId);
        if (gameId != null && events.stream().anyMatch(event -> !goalieId.equals(event.getGoalieId()))) {
            throw new IllegalArgumentException("Game map contains events for a different goalie.");
        }
        List<ShotMapDTO.ShotDTO> shots = events.stream()
                .map(event -> new ShotMapDTO.ShotDTO(event.getGameId(), event.getNhlEventId(),
                        event.getXCoordinate(), event.getYCoordinate(), event.getOutcome()))
                .toList();
        Map<String, List<ShotEvent>> grouped = events.stream().collect(Collectors.groupingBy(this::zone));
        List<ShotMapDTO.ZoneDTO> zones = List.of("INNER_SLOT", "SLOT", "PERIMETER").stream()
                .map(code -> zone(code, grouped.getOrDefault(code, List.of())))
                .toList();
        return new ShotMapDTO(goalieId, seasonId, gameId, shots, zones);
    }

    private ShotMapDTO.ZoneDTO zone(String code, List<ShotEvent> events) {
        int shots = events.size();
        int goals = (int) events.stream().filter(event -> "GOAL".equals(event.getOutcome())).count();
        return new ShotMapDTO.ZoneDTO(code, shots, shots - goals, goals, shots == 0 ? 0 : (double) (shots - goals) / shots);
    }

    private String zone(ShotEvent event) {
        double distance = Math.hypot(89 - Math.abs(event.getXCoordinate()), event.getYCoordinate());
        return distance <= 20 ? "INNER_SLOT" : distance <= 40 ? "SLOT" : "PERIMETER";
    }
}
