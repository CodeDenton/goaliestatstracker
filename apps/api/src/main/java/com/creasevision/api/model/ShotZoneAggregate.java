package com.creasevision.api.model;
import jakarta.persistence.*;
import lombok.Data;
@Data @Entity @Table(name="shot_zone_aggregates") public class ShotZoneAggregate {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private Long goalieId; private String seasonId; private Long gameId; private Integer teamId; private String scope; private String zoneCode; private int shotsAgainst; private int saves; private int goalsAgainst; private Double savePctg; private String sourceName;
}
