package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.repository.GoalieRepository;

@ExtendWith(MockitoExtension.class)
class NhlIngestionServiceTest {

    @Mock
    private NhlGoalieClient nhlGoalieClient;

    @Mock
    private GoalieRepository goalieRepository;

    private NhlIngestionService service;

    @BeforeEach
    void setUp() {
        NhlImportProperties properties = new NhlImportProperties();
        properties.setSeason("20262027");
        properties.setSituation("2");
        service = new NhlIngestionService(nhlGoalieClient, goalieRepository, properties);
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
    }

    @Test
    void reportsFailureWithoutWritingWhenTheClientFails() {
        when(nhlGoalieClient.fetchGoalies("20262027", "2"))
                .thenThrow(new IllegalStateException("NHL is unavailable"));

        NhlIngestionService.ImportResult result = service.refreshCurrentSeason();

        assertThat(result.status()).isEqualTo(NhlIngestionService.Status.FAILED);
        assertThat(result.detail()).isEqualTo("NHL is unavailable");
        verify(goalieRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyIterable());
    }

    private Goalie goalie(long id) {
        Goalie goalie = new Goalie();
        goalie.setId(id);
        return goalie;
    }
}
