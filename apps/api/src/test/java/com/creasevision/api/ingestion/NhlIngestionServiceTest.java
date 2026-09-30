package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.ImportRunRepository;
import com.creasevision.api.repository.SeasonRepository;

@ExtendWith(MockitoExtension.class)
class NhlIngestionServiceTest {

    @Mock
    private NhlGoalieClient nhlGoalieClient;

    @Mock
    private GoalieRepository goalieRepository;
    @Mock private ImportRunRepository importRuns;
    @Mock private SeasonRepository seasons;

    private NhlIngestionService service;

    @BeforeEach
    void setUp() {
        NhlImportProperties properties = new NhlImportProperties();
        properties.setSeason("20262027");
        properties.setSituation("2");
        when(importRuns.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        service = new NhlIngestionService(nhlGoalieClient, goalieRepository, properties, importRuns, seasons);
    }

    @Test
    void savesGoaliesReturnedByTheNhlClient() {
        List<Goalie> goalies = List.of(goalie(1L), goalie(2L));
        when(nhlGoalieClient.fetchGoalies("20262027", "2")).thenReturn(goalies);

        NhlIngestionService.ImportResult result = service.refreshCurrentSeason();

        assertThat(result.status()).isEqualTo(NhlIngestionService.Status.SUCCESS);
        assertThat(result.savedGoalies()).isEqualTo(2);
        verify(goalieRepository).saveAll(goalies);
    }

    @Test
    void preservesExistingDatabaseDataWhenNhlReturnsNoGoalies() {
        when(nhlGoalieClient.fetchGoalies("20262027", "2")).thenReturn(List.of());

        NhlIngestionService.ImportResult result = service.refreshCurrentSeason();

        assertThat(result.status()).isEqualTo(NhlIngestionService.Status.NO_DATA);
        verify(goalieRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyIterable());
        ArgumentCaptor<com.creasevision.api.model.ImportRun> run =
                ArgumentCaptor.forClass(com.creasevision.api.model.ImportRun.class);
        verify(importRuns, times(2)).save(run.capture());
        assertThat(run.getAllValues().getLast().getStatus()).isEqualTo("PARTIAL");
    }

    @Test
    void reportsFailureWithoutWritingWhenTheClientFails() {
        when(nhlGoalieClient.fetchGoalies("20262027", "2"))
                .thenThrow(new IllegalStateException("NHL is unavailable"));

        NhlIngestionService.ImportResult result = service.refreshCurrentSeason();

        assertThat(result.status()).isEqualTo(NhlIngestionService.Status.FAILED);
        assertThat(result.detail()).isEqualTo("NHL is unavailable");
        verify(goalieRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyIterable());
        ArgumentCaptor<com.creasevision.api.model.ImportRun> run =
                ArgumentCaptor.forClass(com.creasevision.api.model.ImportRun.class);
        verify(importRuns, times(2)).save(run.capture());
        assertThat(run.getAllValues().getLast().getStatus()).isEqualTo("FAILED");
    }

    @Test
    void refusesAnOverlappingRefreshWhileTheFirstRefreshIsRunning() throws Exception {
        CountDownLatch clientStarted = new CountDownLatch(1);
        CountDownLatch releaseClient = new CountDownLatch(1);
        when(nhlGoalieClient.fetchGoalies("20262027", "2")).thenAnswer(invocation -> {
            clientStarted.countDown();
            assertThat(releaseClient.await(5, TimeUnit.SECONDS)).isTrue();
            return List.of(goalie(1L));
        });

        CompletableFuture<NhlIngestionService.ImportResult> first =
                CompletableFuture.supplyAsync(service::refreshCurrentSeason);
        assertThat(clientStarted.await(5, TimeUnit.SECONDS)).isTrue();

        NhlIngestionService.ImportResult second = service.refreshCurrentSeason();

        assertThat(second.status()).isEqualTo(NhlIngestionService.Status.IN_PROGRESS);
        verify(goalieRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyIterable());

        releaseClient.countDown();
        assertThat(first.get(5, TimeUnit.SECONDS).status()).isEqualTo(NhlIngestionService.Status.SUCCESS);
    }

    private Goalie goalie(long id) {
        Goalie goalie = new Goalie();
        goalie.setId(id);
        return goalie;
    }
}
