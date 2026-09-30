package com.creasevision.api.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.creasevision.api.ingestion.NhlImportProperties;
import com.creasevision.api.ingestion.NhlIngestionService;
import com.creasevision.api.model.ImportRun;
import com.creasevision.api.repository.ImportRunRepository;

@RestController
@RequestMapping("/api/internal/refresh")
public class RefreshController {
    private static final String SECRET_HEADER = "X-Refresh-Secret";
    private static final String REFRESH_IMPORT_TYPE = "CURRENT_SEASON_REFRESH";

    private final NhlIngestionService ingestion;
    private final NhlImportProperties properties;
    private final ImportRunRepository runs;

    public RefreshController(
            NhlIngestionService ingestion,
            NhlImportProperties properties,
            ImportRunRepository runs) {
        this.ingestion = ingestion;
        this.properties = properties;
        this.runs = runs;
    }

    @PostMapping
    public ResponseEntity<NhlIngestionService.ImportResult> refresh(
            @RequestHeader(value = SECRET_HEADER, required = false) String suppliedSecret) {
        if (!authorized(suppliedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        NhlIngestionService.ImportResult result = ingestion.refreshCurrentSeason();
        HttpStatus status = result.status() == NhlIngestionService.Status.IN_PROGRESS
                ? HttpStatus.ACCEPTED
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    @GetMapping("/latest")
    public ResponseEntity<ImportRun> latest(
            @RequestHeader(value = SECRET_HEADER, required = false) String suppliedSecret) {
        if (!authorized(suppliedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return runs.findTopByImportTypeOrderByStartedAtDesc(REFRESH_IMPORT_TYPE)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean authorized(String suppliedSecret) {
        String expected = properties.getRefreshSecret();
        return expected != null
                && !expected.isBlank()
                && suppliedSecret != null
                && MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
