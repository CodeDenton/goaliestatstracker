package com.creasevision.api.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creasevision.api.model.ShotEvent;

public interface ShotEventRepository extends JpaRepository<ShotEvent, Long> {
    Optional<ShotEvent> findByGameIdAndNhlEventId(Long gameId, Integer nhlEventId);

    List<ShotEvent> findByGoalieIdAndSeasonIdOrderByGameIdAscNhlEventIdAsc(Long goalieId, String seasonId);

    List<ShotEvent> findByGoalieIdAndGameIdOrderByNhlEventIdAsc(Long goalieId, Long gameId);
}
