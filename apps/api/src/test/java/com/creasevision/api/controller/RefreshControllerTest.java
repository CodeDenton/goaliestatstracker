package com.creasevision.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.creasevision.api.ingestion.NhlImportProperties;
import com.creasevision.api.ingestion.NhlIngestionService;
import com.creasevision.api.model.ImportRun;
import com.creasevision.api.repository.ImportRunRepository;

@ExtendWith(MockitoExtension.class)
class RefreshControllerTest {

    @Mock
    private NhlIngestionService ingestion;

    @Mock
    private ImportRunRepository runs;

    private RefreshController controller;

    @BeforeEach
    void setUp() {
        NhlImportProperties properties = new NhlImportProperties();
        properties.setRefreshSecret("test-secret");
        controller = new RefreshController(ingestion, properties, runs);
    }

    @Test
    void rejectsRefreshRequestsWithoutTheConfiguredSecret() {
        assertThat(controller.refresh(null).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(controller.refresh("wrong-secret").getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verify(ingestion, never()).refreshCurrentSeason();
    }

    @Test
    void startsARefreshForAnAuthorizedRequest() {
        NhlIngestionService.ImportResult result =
                NhlIngestionService.ImportResult.success("20262027", 32);
        when(ingestion.refreshCurrentSeason()).thenReturn(result);

        var response = controller.refresh("test-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);
        verify(ingestion).refreshCurrentSeason();
    }

    @Test
    void reportsAnAcceptedRequestWhenAnotherRefreshIsAlreadyRunning() {
        when(ingestion.refreshCurrentSeason())
                .thenReturn(NhlIngestionService.ImportResult.inProgress("20262027"));

        assertThat(controller.refresh("test-secret").getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void protectsLatestRefreshStatusAndReturnsTheMostRecentRun() {
        assertThat(controller.latest(null).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(runs, never()).findTopByImportTypeOrderByStartedAtDesc("CURRENT_SEASON_REFRESH");

        ImportRun run = new ImportRun();
        run.setStatus("SUCCEEDED");
        when(runs.findTopByImportTypeOrderByStartedAtDesc("CURRENT_SEASON_REFRESH"))
                .thenReturn(Optional.of(run));

        var response = controller.latest("test-secret");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(run);
    }
}
