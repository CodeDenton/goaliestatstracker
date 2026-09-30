package com.creasevision.api.ingestion;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "nhl.import-seasons")
public class SeasonBackfillCommand implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SeasonBackfillCommand.class);
    private final SeasonBackfillService importer;
    private final NhlImportProperties properties;

    public SeasonBackfillCommand(SeasonBackfillService importer, NhlImportProperties properties) {
        this.importer = importer;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> seasons = Arrays.stream(properties.getImportSeasons().split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
        SeasonBackfillService.BackfillResult result = importer.importSeasons(seasons);
        log.info("Historical backfill finished: {} succeeded, {} failed.", result.successfulSeasons(), result.failedSeasons());
    }
}
