package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeasonBackfillServiceTest {
    @Mock SeasonImportService seasons;

    @Test
    void importsEveryRequestedSeasonAndReportsIndividualFailures() {
        when(seasons.importSeason("20232024")).thenReturn(new SeasonImportService.SeasonImportResult("20232024", "SUCCEEDED", 100, 100, null));
        when(seasons.importSeason("20242025")).thenReturn(new SeasonImportService.SeasonImportResult("20242025", "FAILED", 0, 0, "NHL unavailable"));

        SeasonBackfillService.BackfillResult result = new SeasonBackfillService(seasons)
                .importSeasons(List.of("20232024", "20242025"));

        assertThat(result.seasons()).hasSize(2);
        assertThat(result.successfulSeasons()).isEqualTo(1);
        assertThat(result.failedSeasons()).isEqualTo(1);
    }
}
