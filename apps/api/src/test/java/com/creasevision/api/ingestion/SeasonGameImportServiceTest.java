package com.creasevision.api.ingestion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class) class SeasonGameImportServiceTest {
 @Mock NhlGoalieClient nhl; @Mock GameImportService games;
 @Test void importsEveryDiscoveredRegularSeasonGameAndReportsFailures(){when(nhl.fetchRegularSeasonGameIds("20252026")).thenReturn(List.of(1L,2L,3L));when(games.importGame(1L)).thenReturn(2);when(games.importGame(2L)).thenThrow(new IllegalStateException());when(games.importGame(3L)).thenReturn(1);var result=new SeasonGameImportService(nhl,games).importSeason("20252026");assertThat(result).isEqualTo(new SeasonGameImportService.Result("20252026",3,3,1));verify(games).importGame(3L);}
}
