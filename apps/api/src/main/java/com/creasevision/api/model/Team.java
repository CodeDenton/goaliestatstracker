package com.creasevision.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "teams")
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    private String abbreviation;
    private String commonName;
    private String logoLightUrl;
    private String logoDarkUrl;
    private boolean active = true;
}
