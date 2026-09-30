package com.creasevision.api.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data @Entity @Table(name = "import_runs")
public class ImportRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String importType;
    private String seasonId;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
    private String status;
    private int recordsRead;
    private int recordsWritten;
    private String errorDetail;
}
