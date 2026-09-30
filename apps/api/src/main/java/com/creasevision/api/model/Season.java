package com.creasevision.api.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "seasons")
public class Season {
    @Id private String id;
    private String displayName;
    private LocalDate startsOn;
    private LocalDate endsOn;
    private boolean isCurrent;
}
