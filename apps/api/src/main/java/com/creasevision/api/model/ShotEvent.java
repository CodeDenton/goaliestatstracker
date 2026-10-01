package com.creasevision.api.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "shot_events")
public class ShotEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long gameId;
    private String seasonId;
    private Integer nhlEventId;
    private Long goalieId;
    private Integer defendingTeamId;
    private Integer xCoordinate;
    private Integer yCoordinate;
    private String outcome;
    private String sourceName;
    private OffsetDateTime sourceUpdatedAt;
}
