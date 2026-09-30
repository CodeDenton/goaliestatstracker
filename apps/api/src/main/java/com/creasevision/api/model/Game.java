package com.creasevision.api.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "games")
public class Game {
    @Id private Long id;
    private String seasonId;
    private LocalDate gameDate;
    private short gameType;
    private Integer homeTeamId;
    private Integer awayTeamId;
    private Integer homeScore;
    private Integer awayScore;
    private String gameState;
    private String sourceName;
}
