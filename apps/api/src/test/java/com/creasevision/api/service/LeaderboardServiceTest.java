package com.creasevision.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.GoalieSeasonStats;
import com.creasevision.api.model.GoalieTeamStint;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.GoalieSeasonStatsRepository;
import com.creasevision.api.repository.GoalieTeamStintRepository;
import com.creasevision.api.repository.TeamRepository;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {
    @Mock private GoalieRepository goalies;
    @Mock private GoalieTeamStintRepository stints;
    @Mock private GoalieSeasonStatsRepository stats;
    @Mock private TeamRepository teams;

    @Test
    void aggregatesTradedGoalieStintsThenFiltersSortsAndPaginatesTheSeasonLeaderboard() {
        GoalieTeamStint jarryPit = stint(1L, 31L, 5);
        GoalieTeamStint jarryEdm = stint(2L, 31L, 22);
        GoalieTeamStint skinnerEdm = stint(3L, 40L, 22);
        when(stints.findBySeasonId("20252026")).thenReturn(List.of(jarryPit, jarryEdm, skinnerEdm));
        when(stats.findByTeamStintIdIn(List.of(1L, 2L, 3L))).thenReturn(List.of(
                line(1L, 20, 12, 6, 2, 530, 575),
                line(2L, 10, 5, 4, 1, 260, 280),
                line(3L, 35, 21, 9, 3, 900, 980)));
        when(goalies.findAllById(List.of(31L, 40L))).thenReturn(List.of(goalie(31L, "Tristan", "Jarry"), goalie(40L, "Stuart", "Skinner")));
        when(teams.findById(5)).thenReturn(Optional.of(team(5, "PIT")));
        when(teams.findById(22)).thenReturn(Optional.of(team(22, "EDM")));

        LeaderboardService.LeaderboardPage page = new LeaderboardService(goalies, stints, stats, teams)
                .leaderboard("20252026", null, 25, "savePctg", "desc", 0, 1, null);

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.rows()).singleElement().satisfies(row -> {
            assertThat(row.goalieId()).isEqualTo(31L);
            assertThat(row.teamAbbreviations()).containsExactly("EDM", "PIT");
            assertThat(row.gamesPlayed()).isEqualTo(30);
            assertThat(row.wins()).isEqualTo(17);
            assertThat(row.savePctg()).isEqualTo((530d + 260d) / (575d + 280d));
        });
    }

    private GoalieTeamStint stint(long id, long goalieId, int teamId) {
        GoalieTeamStint stint = new GoalieTeamStint(); stint.setId(id); stint.setGoalieId(goalieId); stint.setTeamId(teamId); return stint;
    }
    private GoalieSeasonStats line(long stintId, int games, int wins, int losses, int ot, int saves, int shots) {
        GoalieSeasonStats line = new GoalieSeasonStats(); line.setTeamStintId(stintId); line.setGamesPlayed(games); line.setWins(wins); line.setLosses(losses); line.setOvertimeLosses(ot); line.setSaves(saves); line.setShotsAgainst(shots); return line;
    }
    private Goalie goalie(long id, String first, String last) { Goalie goalie = new Goalie(); goalie.setId(id); goalie.setFirstName(first); goalie.setLastName(last); return goalie; }
    private Team team(int id, String abbreviation) { Team team = new Team(); team.setId(id); team.setAbbreviation(abbreviation); return team; }
}
