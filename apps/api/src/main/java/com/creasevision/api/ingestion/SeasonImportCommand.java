package com.creasevision.api.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Explicit one-off import entry point. It is disabled during normal API
 * startup, preventing deployments or Render cold starts from backfilling data.
 */
@Component
@ConditionalOnProperty(name = "nhl.import-season")
public class SeasonImportCommand implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SeasonImportCommand.class);
    private final SeasonImportService importer;
    private final NhlImportProperties properties;

    public SeasonImportCommand(SeasonImportService importer, NhlImportProperties properties) {
        this.importer = importer;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        SeasonImportService.SeasonImportResult result = importer.importSeason(properties.getImportSeason());
        log.info("Season import {} finished with {}: {}/{} records written.", result.seasonId(),
                result.status(), result.recordsWritten(), result.recordsRead());
    }
}
