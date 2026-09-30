package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        lenient().when(teams.save(any())).thenAnswer(i->{Team t=i.getArgument(0);if(t==null)return null;t.setId(7);return t;});
        lenient().when(stints.save(any())).thenAnswer(i->{GoalieTeamStint s=i.getArgument(0);if(s==null)return null;if(s.getId()==null)s.setId(11L);return s;});
        lenient().when(stats.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void createsOneStintForEachTeamSplitOfATradedGoalie() {
        NhlGoalieSeasonRow skinnerEdmonton=row(8479973L,"EDM",23,11,8,4,508,570,2.82535,.89122);
        NhlGoalieSeasonRow skinnerPittsburgh=row(8479973L,"PIT",27,12,9,5,617,697,2.99009,.88522);
        when(nhl.fetchRegularSeasonTeamSplits("20252026")).thenReturn(List.of(skinnerEdmonton,skinnerPittsburgh));
        when(teams.findByAbbreviation(any())).thenReturn(Optional.empty());
        when(teams.save(any())).thenAnswer(i->{Team t=i.getArgument(0);t.setId(t.getAbbreviation().equals("EDM")?7:8);return t;});
        when(stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(8479973L,"20252026",7,(short)1)).thenReturn(Optional.empty());
        when(stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(8479973L,"20252026",8,(short)1)).thenReturn(Optional.empty());
        when(stints.save(any())).thenAnswer(i->{GoalieTeamStint s=i.getArgument(0);s.setId(s.getTeamId()==7?11L:12L);return s;});
        when(stats.findByTeamStintId(any())).thenReturn(Optional.empty());
        SeasonImportService.SeasonImportResult result=service.importSeason("20252026");
        assertThat(result.status()).isEqualTo("SUCCEEDED"); assertThat(result.recordsWritten()).isEqualTo(2);
        verify(profiles,times(2)).saveProfile(any(Goalie.class)); verify(stints,times(2)).save(any(GoalieTeamStint.class));
        org.mockito.ArgumentCaptor<GoalieSeasonStats> lines=org.mockito.ArgumentCaptor.forClass(GoalieSeasonStats.class);
        verify(stats,times(2)).save(lines.capture());
        assertThat(lines.getAllValues()).extracting(GoalieSeasonStats::getSaves).containsExactlyInAnyOrder(508,617);
        assertThat(lines.getAllValues()).extracting(GoalieSeasonStats::getShotsAgainst).containsExactlyInAnyOrder(570,697);
    }
    @Test void reimportingTeamSplitsUpdatesExistingStintsWithoutCreatingDuplicates() {
        NhlGoalieSeasonRow jarryPittsburgh=row(8477465L,"PIT",14,9,3,1,360,396,2.66419,.90909);
        NhlGoalieSeasonRow jarryEdmonton=row(8477465L,"EDM",19,9,6,2,374,436,3.86346,.85779);
        when(nhl.fetchRegularSeasonTeamSplits("20252026")).thenReturn(List.of(jarryPittsburgh,jarryEdmonton));
        Team pit=new Team();pit.setId(7);pit.setAbbreviation("PIT"); Team edm=new Team();edm.setId(8);edm.setAbbreviation("EDM");
        when(teams.findByAbbreviation("PIT")).thenReturn(Optional.of(pit)); when(teams.findByAbbreviation("EDM")).thenReturn(Optional.of(edm));
        when(teams.save(any())).thenAnswer(i->i.getArgument(0));
        GoalieTeamStint pitStint=new GoalieTeamStint();pitStint.setId(11L); GoalieTeamStint edmStint=new GoalieTeamStint();edmStint.setId(12L);
        when(stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(8477465L,"20252026",7,(short)1)).thenReturn(Optional.of(pitStint));
        when(stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(8477465L,"20252026",8,(short)1)).thenReturn(Optional.of(edmStint));
        when(stats.findByTeamStintId(11L)).thenReturn(Optional.of(new GoalieSeasonStats())); when(stats.findByTeamStintId(12L)).thenReturn(Optional.of(new GoalieSeasonStats()));
        SeasonImportService.SeasonImportResult first=service.importSeason("20252026");
        SeasonImportService.SeasonImportResult second=service.importSeason("20252026");
        assertThat(first.recordsWritten()).isEqualTo(2); assertThat(second.recordsWritten()).isEqualTo(2);
        verify(stints,never()).save(argThat(stint -> stint.getId()==null));
        verify(stats,times(4)).save(any(GoalieSeasonStats.class));
    }
    @Test void recordsFailureWhenNhlReturnsNoTeamSplits() {
        when(nhl.fetchRegularSeasonTeamSplits("20232024")).thenReturn(List.of());
        SeasonImportService.SeasonImportResult result=service.importSeason("20232024");
        assertThat(result.status()).isEqualTo("FAILED"); assertThat(result.errorDetail()).contains("no goalies"); verify(profiles,never()).saveProfile(any());
    }
    @Test void rejectsMalformedSeasonBeforeCallingNhl() {
        org.assertj.core.api.Assertions.assertThatThrownBy(()->service.importSeason("2024")).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(nhl,runs);
    }
    private NhlGoalieSeasonRow row(long goalieId,String team,int games,int wins,int losses,int otl,int saves,int shots,double gaa,double savePct) {
        return new NhlGoalieSeasonRow(goalieId,"Fixture","Goalie",team,team,games,wins,losses,otl,saves,shots,gaa,savePct);
    }
}
