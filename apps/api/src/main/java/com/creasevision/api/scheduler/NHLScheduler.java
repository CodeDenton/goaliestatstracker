package com.creasevision.api.scheduler;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.creasevision.api.ingestion.NhlImportProperties;
import com.creasevision.api.ingestion.NhlIngestionService;

@Component
public class NHLScheduler {

    private final NhlIngestionService ingestionService;
    private final NhlImportProperties properties;

    public NHLScheduler(NhlIngestionService ingestionService, NhlImportProperties properties) {
        this.ingestionService = ingestionService;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void refreshOnStartup() {
        if (properties.isRefreshOnStartup()) {
            ingestionService.refreshCurrentSeason();
        }
    }

    /** Runs nightly at 1:00 AM Vancouver time, after the prior NHL night is complete. */
    @Scheduled(cron = "${nhl.refresh.cron}", zone = "${nhl.refresh.zone}")
    public void refreshNightly() {
        ingestionService.refreshCurrentSeason();
    }
}
