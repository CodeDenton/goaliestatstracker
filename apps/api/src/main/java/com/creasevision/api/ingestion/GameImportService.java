package com.creasevision.api.ingestion;

import java.util.List;

import org.springframework.stereotype.Service;

import com.creasevision.api.model.Game;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.model.ShotZoneAggregate;
import com.creasevision.api.model.ShotEvent;
import com.creasevision.api.model.Team;
import com.creasevision.api.model.Season;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.SeasonRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.repository.TeamRepository;
import com.creasevision.api.repository.ShotZoneAggregateRepository;
import com.creasevision.api.repository.ShotEventRepository;
import java.time.OffsetDateTime;

@Service
public class GameImportService {
    private final NhlGoalieClient nhl; private final GameRepository games; private final SeasonRepository seasons; private final TeamRepository teams; private final GoalieGameStatsRepository goalieStats; private final GoalieProfileStore profiles; private final ShotZoneAggregateRepository zones; private final ShotEventRepository shotEvents;
    public GameImportService(NhlGoalieClient nhl, GameRepository games, SeasonRepository seasons, TeamRepository teams, GoalieGameStatsRepository goalieStats, GoalieProfileStore profiles, ShotZoneAggregateRepository zones, ShotEventRepository shotEvents) { this.nhl=nhl; this.games=games; this.seasons=seasons; this.teams=teams; this.goalieStats=goalieStats; this.profiles=profiles; this.zones=zones; this.shotEvents=shotEvents; }
    public int importGame(long gameId) {
        List<NhlGoalieGameRow> rows=nhl.fetchGoalieGameRows(gameId); if(rows.isEmpty()) return 0;
        NhlGoalieGameRow first=rows.getFirst(); season(first.seasonId()); Team home=team(first.homeTeam()); Team away=team(first.awayTeam());
        Game game=games.findById(gameId).orElseGet(Game::new); game.setId(gameId); game.setSeasonId(first.seasonId()); game.setGameDate(first.gameDate()); game.setGameType(first.gameType()); game.setHomeTeamId(home.getId()); game.setAwayTeamId(away.getId()); game.setHomeScore(first.homeScore()); game.setAwayScore(first.awayScore()); game.setGameState(first.gameState()); game.setSourceName("NHL API"); games.save(game);
        for(NhlGoalieGameRow row:rows) { com.creasevision.api.model.Goalie goalie=new com.creasevision.api.model.Goalie(); goalie.setId(row.goalieId()); profiles.saveProfile(goalie); Team team=team(row.team()); GoalieGameStats stat=goalieStats.findByGameIdAndGoalieId(gameId,row.goalieId()).orElseGet(GoalieGameStats::new); stat.setGameId(gameId); stat.setGoalieId(row.goalieId()); stat.setTeamId(team.getId()); stat.setIsStarter(row.starter()); stat.setDecision(row.decision()); stat.setTimeOnIceSeconds(row.timeOnIceSeconds()); stat.setShotsAgainst(row.shotsAgainst()); stat.setSaves(row.saves()); stat.setGoalsAgainst(row.goalsAgainst()); stat.setSavePctg(row.savePctg()); stat.setSourceName("NHL API"); goalieStats.save(stat); }
        for(NhlShotZoneRow row:nhl.fetchShotZoneRows(gameId)) { Team team=team(row.team()); ShotZoneAggregate zone=zones.findByGoalieIdAndSeasonIdAndGameIdAndTeamIdAndScopeAndZoneCode(row.goalieId(),row.seasonId(),gameId,team.getId(),"GAME",row.zoneCode()).orElseGet(ShotZoneAggregate::new); zone.setGoalieId(row.goalieId()); zone.setSeasonId(row.seasonId()); zone.setGameId(gameId); zone.setTeamId(team.getId()); zone.setScope("GAME"); zone.setZoneCode(row.zoneCode()); zone.setShotsAgainst(row.shots()); zone.setSaves(row.saves()); zone.setGoalsAgainst(row.goals()); zone.setSavePctg(row.shots()==0?null:(double)row.saves()/row.shots()); zone.setSourceName("NHL API"); zones.save(zone); }
        for(NhlShotEventRow row:nhl.fetchShotEvents(gameId)) { Team team=team(row.defendingTeam()); ShotEvent event=shotEvents.findByGameIdAndNhlEventId(gameId,row.eventId()).orElseGet(ShotEvent::new); event.setGameId(gameId); event.setSeasonId(row.seasonId()); event.setNhlEventId(row.eventId()); event.setGoalieId(row.goalieId()); event.setDefendingTeamId(team.getId()); event.setXCoordinate(row.xCoordinate()); event.setYCoordinate(row.yCoordinate()); event.setOutcome(row.goal()?"GOAL":"SAVE"); event.setSourceName("NHL API"); event.setSourceUpdatedAt(OffsetDateTime.now()); shotEvents.save(event); }
        return rows.size();
    }
    private Team team(String abbreviation) { Team team=teams.findByAbbreviation(abbreviation).orElseGet(Team::new); team.setAbbreviation(abbreviation); if(team.getCommonName()==null) team.setCommonName(abbreviation); return teams.save(team); }
    private void season(String id) { if (!seasons.existsById(id)) { int year=Integer.parseInt(id.substring(0,4)); Season season=new Season(); season.setId(id); season.setDisplayName(year+"-"+String.format("%02d",(year+1)%100)); season.setStartsOn(java.time.LocalDate.of(year,10,1)); season.setEndsOn(java.time.LocalDate.of(year+1,6,30)); season.setCurrent(false); seasons.save(season); } }
}
