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
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.GoalieSeasonStatsRepository;
import com.creasevision.api.repository.GoalieTeamStintRepository;

@ExtendWith(MockitoExtension.class)
class GoalieProfileServiceTest {
    @Mock GoalieRepository goalies; @Mock GoalieTeamStintRepository stints; @Mock GoalieSeasonStatsRepository stats;

    @Test void returnsCareerSeasonsForOneGoalieWithoutLoadingTheLeague() {
        Goalie goalie=new Goalie(); goalie.setId(31L); goalie.setFirstName("Tristan"); goalie.setLastName("Jarry");
        GoalieTeamStint stint=new GoalieTeamStint(); stint.setId(9L); stint.setSeasonId("20252026"); stint.setGoalieId(31L);
        GoalieSeasonStats line=new GoalieSeasonStats(); line.setTeamStintId(9L); line.setGamesPlayed(30); line.setWins(17); line.setSaves(790); line.setShotsAgainst(855);
        when(goalies.findById(31L)).thenReturn(Optional.of(goalie)); when(stints.findByGoalieIdOrderBySeasonIdDesc(31L)).thenReturn(List.of(stint)); when(stats.findByTeamStintIdIn(List.of(9L))).thenReturn(List.of(line));
        var profile=new GoalieProfileService(goalies,stints,stats).profile(31L);
        assertThat(profile.name()).isEqualTo("Tristan Jarry"); assertThat(profile.seasons()).singleElement().satisfies(season->{assertThat(season.seasonId()).isEqualTo("20252026");assertThat(season.gamesPlayed()).isEqualTo(30);assertThat(season.savePctg()).isEqualTo(790d/855);});
    }
}
