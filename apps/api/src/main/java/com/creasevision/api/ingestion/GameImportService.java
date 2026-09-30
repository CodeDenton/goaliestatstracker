package com.creasevision.api.ingestion;

import java.util.List;

import org.springframework.stereotype.Service;

import com.creasevision.api.model.Game;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.model.ShotZoneAggregate;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.repository.TeamRepository;
import com.creasevision.api.repository.ShotZoneAggregateRepository;

@Service
public class GameImportService {
    private final NhlGoalieClient nhl; private final GameRepository games; private final TeamRepository teams; private final GoalieGameStatsRepository goalieStats; private final GoalieProfileStore profiles; private final ShotZoneAggregateRepository zones;
    public GameImportService(NhlGoalieClient nhl, GameRepository games, TeamRepository teams, GoalieGameStatsRepository goalieStats, GoalieProfileStore profiles, ShotZoneAggregateRepository zones) { this.nhl=nhl; this.games=games; this.teams=teams; this.goalieStats=goalieStats; this.profiles=profiles; this.zones=zones; }
    public int importGame(long gameId) {
        List<NhlGoalieGameRow> rows=nhl.fetchGoalieGameRows(gameId); if(rows.isEmpty()) return 0;
        NhlGoalieGameRow first=rows.getFirst(); Team home=team(first.homeTeam()); Team away=team(first.awayTeam());
        Game game=games.findById(gameId).orElseGet(Game::new); game.setId(gameId); game.setSeasonId(first.seasonId()); game.setGameDate(first.gameDate()); game.setGameType(first.gameType()); game.setHomeTeamId(home.getId()); game.setAwayTeamId(away.getId()); game.setHomeScore(first.homeScore()); game.setAwayScore(first.awayScore()); game.setGameState(first.gameState()); game.setSourceName("NHL API"); games.save(game);
        for(NhlGoalieGameRow row:rows) { com.creasevision.api.model.Goalie goalie=new com.creasevision.api.model.Goalie(); goalie.setId(row.goalieId()); profiles.saveProfile(goalie); Team team=team(row.team()); GoalieGameStats stat=goalieStats.findByGameIdAndGoalieId(gameId,row.goalieId()).orElseGet(GoalieGameStats::new); stat.setGameId(gameId); stat.setGoalieId(row.goalieId()); stat.setTeamId(team.getId()); stat.setIsStarter(row.starter()); stat.setDecision(row.decision()); stat.setTimeOnIceSeconds(row.timeOnIceSeconds()); stat.setShotsAgainst(row.shotsAgainst()); stat.setSaves(row.saves()); stat.setGoalsAgainst(row.goalsAgainst()); stat.setSavePctg(row.savePctg()); stat.setSourceName("NHL API"); goalieStats.save(stat); }
        for(NhlShotZoneRow row:nhl.fetchShotZoneRows(gameId)) { Team team=team(row.team()); ShotZoneAggregate zone=zones.findByGoalieIdAndSeasonIdAndGameIdAndTeamIdAndScopeAndZoneCode(row.goalieId(),row.seasonId(),gameId,team.getId(),"GAME",row.zoneCode()).orElseGet(ShotZoneAggregate::new); zone.setGoalieId(row.goalieId()); zone.setSeasonId(row.seasonId()); zone.setGameId(gameId); zone.setTeamId(team.getId()); zone.setScope("GAME"); zone.setZoneCode(row.zoneCode()); zone.setShotsAgainst(row.shots()); zone.setSaves(row.saves()); zone.setGoalsAgainst(row.goals()); zone.setSavePctg(row.shots()==0?null:(double)row.saves()/row.shots()); zone.setSourceName("NHL API"); zones.save(zone); }
        return rows.size();
    }
    private Team team(String abbreviation) { Team team=teams.findByAbbreviation(abbreviation).orElseGet(Team::new); team.setAbbreviation(abbreviation); if(team.getCommonName()==null) team.setCommonName(abbreviation); return teams.save(team); }
}
