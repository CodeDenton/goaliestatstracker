package com.creasevision.api.ingestion;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

/** Runs explicit, resumable historical imports without changing normal startup behavior. */
@Service
public class SeasonBackfillService {
    private final SeasonImportService seasons;

    public SeasonBackfillService(SeasonImportService seasons) {
        this.seasons = seasons;
    }

    public BackfillResult importSeasons(List<String> seasonIds) {
        List<SeasonImportService.SeasonImportResult> results = new ArrayList<>();
        for (String seasonId : seasonIds) results.add(seasons.importSeason(seasonId));
        return new BackfillResult(results);
    }

    public record BackfillResult(List<SeasonImportService.SeasonImportResult> seasons) {
        public long successfulSeasons() {
            return seasons.stream().filter(result -> "SUCCEEDED".equals(result.status())).count();
        }

        public long failedSeasons() {
            return seasons.size() - successfulSeasons();
        }
    }
}
