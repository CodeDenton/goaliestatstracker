package com.creasevision.api.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "goalie_team_stints")
public class GoalieTeamStint {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long goalieId;
    private String seasonId;
    private Integer teamId;
    private short stintNumber = 1;
    private Integer sweaterNumber;
    private String sourceName;
    private OffsetDateTime sourceUpdatedAt;
}
