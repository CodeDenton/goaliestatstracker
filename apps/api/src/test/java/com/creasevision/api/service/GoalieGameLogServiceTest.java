package com.creasevision.api.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.creasevision.api.model.Game;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.repository.TeamRepository;
@ExtendWith(MockitoExtension.class) class GoalieGameLogServiceTest {
 @Mock GoalieGameStatsRepository stats; @Mock GameRepository games; @Mock TeamRepository teams;
 @Test void returnsNewestEnrichedRequestedPage(){GoalieGameStats stat=new GoalieGameStats();stat.setGameId(2L);stat.setTeamId(1);stat.setDecision("W");when(stats.findByGoalieIdNewestFirst(31L)).thenReturn(List.of(stat));Game game=new Game();game.setId(2L);game.setGameDate(LocalDate.of(2026,4,10));game.setHomeTeamId(1);game.setAwayTeamId(2);game.setHomeScore(3);game.setAwayScore(2);when(games.findAllById(List.of(2L))).thenReturn(List.of(game));Team pit=new Team();pit.setId(1);pit.setAbbreviation("PIT");Team bos=new Team();bos.setId(2);bos.setAbbreviation("BOS");when(teams.findAllById(List.of(1,2))).thenReturn(List.of(pit,bos));var page=new GoalieGameLogService(stats,games,teams).games(31L,0,1);assertThat(page.rows()).singleElement().extracting(GoalieGameLogService.Row::gameDate,GoalieGameLogService.Row::opponent,GoalieGameLogService.Row::teamScore,GoalieGameLogService.Row::decision).containsExactly(LocalDate.of(2026,4,10),"BOS",3,"W");}
}
