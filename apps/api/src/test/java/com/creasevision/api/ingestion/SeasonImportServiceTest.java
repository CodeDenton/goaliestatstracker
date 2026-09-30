package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.model.*;
import com.creasevision.api.repository.*;

@ExtendWith(MockitoExtension.class)
class SeasonImportServiceTest {
    @Mock NhlGoalieClient nhl; @Mock GoalieProfileStore profiles; @Mock SeasonRepository seasons; @Mock TeamRepository teams;
    @Mock GoalieTeamStintRepository stints; @Mock GoalieSeasonStatsRepository stats; @Mock ImportRunRepository runs;
    private SeasonImportService service;
    @BeforeEach void setUp() {
        NhlImportProperties config=new NhlImportProperties(); config.setSeason("20262027");
        service=new SeasonImportService(nhl,config,profiles,seasons,teams,stints,stats,runs);
        lenient().when(runs.save(any())).thenAnswer(i->i.getArgument(0));
        lenient().when(teams.save(any())).thenAnswer(i->{Team t=i.getArgument(0);t.setId(7);return t;});
        lenient().when(stints.save(any())).thenAnswer(i->{GoalieTeamStint s=i.getArgument(0);s.setId(11L);return s;});
        lenient().when(stats.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void createsSeasonStintStatsAndSuccessfulAuditForImportedGoalie() {
        Goalie goalie=new Goalie(); goalie.setId(99L); goalie.setTeamAbbrev("BOS"); goalie.setTeamName("Bruins"); goalie.setGamesPlayed(10); goalie.setWins(6); goalie.setLosses(3); goalie.setOvertimeLosses(1);
        when(nhl.fetchGoalies("20232024","2")).thenReturn(List.of(goalie)); when(teams.findByAbbreviation("BOS")).thenReturn(Optional.empty()); when(stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(99L,"20232024",7,(short)1)).thenReturn(Optional.empty()); when(stats.findByTeamStintId(11L)).thenReturn(Optional.empty());
        SeasonImportService.SeasonImportResult result=service.importSeason("20232024");
        assertThat(result.status()).isEqualTo("SUCCEEDED"); assertThat(result.recordsWritten()).isEqualTo(1);
        verify(profiles).saveProfile(goalie); verify(seasons).save(any(Season.class)); verify(stints).save(any(GoalieTeamStint.class)); verify(stats).save(any(GoalieSeasonStats.class));
    }
    @Test void rejectsMalformedSeasonBeforeCallingNhl() {
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.importSeason("2024")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(nhl,runs);
    }
    @Test void recordsFailureWhenNhlReturnsNoGoalies() {
        when(nhl.fetchGoalies("20232024","2")).thenReturn(List.of());
        SeasonImportService.SeasonImportResult result=service.importSeason("20232024");
        assertThat(result.status()).isEqualTo("FAILED"); assertThat(result.errorDetail()).contains("no goalies"); verify(profiles,never()).saveProfile(any());
    }
}
