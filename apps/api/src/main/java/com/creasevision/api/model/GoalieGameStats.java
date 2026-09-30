package com.creasevision.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "goalie_game_stats")
public class GoalieGameStats {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long gameId;
    private Long goalieId;
    private Integer teamId;
    private Boolean isStarter;
    private String decision;
    private Integer timeOnIceSeconds;
    private Integer shotsAgainst;
    private Integer saves;
    private Integer goalsAgainst;
    private Double savePctg;
    private String sourceName;
}
