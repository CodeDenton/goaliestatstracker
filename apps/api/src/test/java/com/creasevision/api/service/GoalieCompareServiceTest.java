package com.creasevision.api.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.creasevision.api.model.*;
import com.creasevision.api.repository.*;
@ExtendWith(MockitoExtension.class) class GoalieCompareServiceTest {
 @Mock GoalieRepository goalies; @Mock GoalieTeamStintRepository stints; @Mock GoalieSeasonStatsRepository stats;
 @Test void sumsMultipleTeamStints(){Goalie goalie=new Goalie();goalie.setId(1L);goalie.setFirstName("A");goalie.setLastName("Goalie");GoalieTeamStint a=new GoalieTeamStint();a.setId(10L);GoalieTeamStint b=new GoalieTeamStint();b.setId(11L);GoalieSeasonStats x=new GoalieSeasonStats();x.setGamesPlayed(10);x.setSaves(270);x.setShotsAgainst(300);GoalieSeasonStats y=new GoalieSeasonStats();y.setGamesPlayed(5);y.setSaves(140);y.setShotsAgainst(150);when(goalies.findById(1L)).thenReturn(java.util.Optional.of(goalie));when(stints.findByGoalieIdAndSeasonId(1L,"20252026")).thenReturn(List.of(a,b));when(stats.findByTeamStintIdIn(List.of(10L,11L))).thenReturn(List.of(x,y));Goalie other=new Goalie();other.setId(2L);other.setFirstName("B");other.setLastName("Goalie");when(goalies.findById(2L)).thenReturn(java.util.Optional.of(other));when(stints.findByGoalieIdAndSeasonId(2L,"20252026")).thenReturn(List.of());when(stats.findByTeamStintIdIn(List.of())).thenReturn(List.of());var result=new GoalieCompareService(goalies,stints,stats).compare("20252026",List.of(1L,2L));assertThat(result.goalies().getFirst()).extracting(GoalieCompareService.Row::gamesPlayed,GoalieCompareService.Row::savePctg).containsExactly(15,410d/450);}
}
