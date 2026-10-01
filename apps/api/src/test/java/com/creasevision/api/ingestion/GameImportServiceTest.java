package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.creasevision.api.model.Game;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.SeasonRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.repository.TeamRepository;
import com.creasevision.api.repository.ShotZoneAggregateRepository;
import com.creasevision.api.model.ShotZoneAggregate;
import com.creasevision.api.model.ShotEvent;
import com.creasevision.api.repository.ShotEventRepository;

@ExtendWith(MockitoExtension.class)
class GameImportServiceTest {
    @Mock NhlGoalieClient nhl; @Mock GameRepository games; @Mock SeasonRepository seasons; @Mock TeamRepository teams; @Mock GoalieGameStatsRepository goalieStats; @Mock GoalieProfileStore profiles; @Mock ShotZoneAggregateRepository zones; @Mock ShotEventRepository shotEvents;
    private GameImportService service;

    @BeforeEach void setUp() {
        service=new GameImportService(nhl,games,seasons,teams,goalieStats,profiles,zones,shotEvents);
        when(teams.save(any())).thenAnswer(i->{Team team=i.getArgument(0); if(team==null)return null; team.setId(team.getAbbreviation().equals("PIT")?5:22); return team;});
        when(games.save(any())).thenAnswer(i->i.getArgument(0)); when(goalieStats.save(any())).thenAnswer(i->i.getArgument(0));
    }

    @Test void createsGameAndOneUpsertedStatLinePerGoalie() {
        NhlGoalieGameRow jarry=row(8477465L,"PIT","W",25,27); NhlGoalieGameRow skinner=row(8479973L,"EDM","L",28,31);
        when(nhl.fetchGoalieGameRows(2025020001L)).thenReturn(List.of(jarry,skinner));
        when(nhl.fetchShotZoneRows(2025020001L)).thenReturn(List.of(new NhlShotZoneRow(2025020001L,"20252026",8477465L,"PIT","INNER_SLOT",3,2,1)));
        when(nhl.fetchShotEvents(2025020001L)).thenReturn(List.of(new NhlShotEventRow(2025020001L,"20252026",101,8477465L,"PIT",80,4,false)));
        when(teams.findByAbbreviation(any())).thenReturn(Optional.empty()); when(games.findById(2025020001L)).thenReturn(Optional.empty());
        when(goalieStats.findByGameIdAndGoalieId(any(),any())).thenReturn(Optional.empty());
        when(zones.findByGoalieIdAndSeasonIdAndGameIdAndTeamIdAndScopeAndZoneCode(any(),any(),any(),any(),any(),any())).thenReturn(Optional.empty()); when(zones.save(any())).thenAnswer(i->i.getArgument(0));
        when(shotEvents.findByGameIdAndNhlEventId(any(),any())).thenReturn(Optional.empty()); when(shotEvents.save(any())).thenAnswer(i->i.getArgument(0));
        assertThat(service.importGame(2025020001L)).isEqualTo(2);
        org.mockito.ArgumentCaptor<Game> game=org.mockito.ArgumentCaptor.forClass(Game.class); verify(games).save(game.capture());
        assertThat(game.getValue().getSeasonId()).isEqualTo("20252026"); assertThat(game.getValue().getHomeTeamId()).isEqualTo(5);
        org.mockito.ArgumentCaptor<GoalieGameStats> stats=org.mockito.ArgumentCaptor.forClass(GoalieGameStats.class); verify(goalieStats,times(2)).save(stats.capture());
        assertThat(stats.getAllValues()).extracting(GoalieGameStats::getGoalieId).containsExactlyInAnyOrder(8477465L,8479973L);
        assertThat(stats.getAllValues()).extracting(GoalieGameStats::getSaves).containsExactlyInAnyOrder(25,28);
        verify(profiles,times(2)).saveProfile(any());
        org.mockito.ArgumentCaptor<ShotZoneAggregate> zone=org.mockito.ArgumentCaptor.forClass(ShotZoneAggregate.class); verify(zones).save(zone.capture());
        assertThat(zone.getValue()).extracting(ShotZoneAggregate::getZoneCode,ShotZoneAggregate::getShotsAgainst,ShotZoneAggregate::getSaves,ShotZoneAggregate::getGoalsAgainst).containsExactly("INNER_SLOT",3,2,1);
        org.mockito.ArgumentCaptor<ShotEvent> event=org.mockito.ArgumentCaptor.forClass(ShotEvent.class); verify(shotEvents).save(event.capture());
        assertThat(event.getValue()).extracting(ShotEvent::getNhlEventId,ShotEvent::getXCoordinate,ShotEvent::getYCoordinate,ShotEvent::getOutcome).containsExactly(101,80,4,"SAVE");
    }

    private NhlGoalieGameRow row(long goalieId,String team,String decision,int saves,int shots) {
        return new NhlGoalieGameRow(2025020001L,"20252026",LocalDate.of(2025,10,7),(short)2,"PIT","EDM",3,2,"OFF",goalieId,team,true,decision,3600,shots,saves,shots-saves,(double)saves/shots);
    }
}
