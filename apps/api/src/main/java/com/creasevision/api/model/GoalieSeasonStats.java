package com.creasevision.api.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "goalie_season_stats")
public class GoalieSeasonStats {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long teamStintId;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int overtimeLosses;
    private Integer shotsAgainst;
    private Integer saves;
    private Double goalsAgainstAvg;
    private Double savePctg;
    private String sourceName;
    private OffsetDateTime sourceUpdatedAt;
}
