package com.creasevision.api.service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.repository.GoalieGameStatsRepository;
import com.creasevision.api.model.Game;
import com.creasevision.api.model.Team;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.TeamRepository;
@Service public class GoalieGameLogService {
    private final GoalieGameStatsRepository stats; private final GameRepository games; private final TeamRepository teams;
    public GoalieGameLogService(GoalieGameStatsRepository stats,GameRepository games,TeamRepository teams){this.stats=stats;this.games=games;this.teams=teams;}
    @Transactional(readOnly=true) public Page games(Long goalieId,int page,int size){
        if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid game-log paging.");
        List<GoalieGameStats> all=stats.findByGoalieIdNewestFirst(goalieId);
        Map<Long,Game> gameById=games.findAllById(all.stream().map(GoalieGameStats::getGameId).toList()).stream().collect(Collectors.toMap(Game::getId,Function.identity()));
        Map<Integer,Team> teamById=teams.findAllById(gameById.values().stream().flatMap(g->java.util.stream.Stream.of(g.getHomeTeamId(),g.getAwayTeamId())).distinct().toList()).stream().collect(Collectors.toMap(Team::getId,Function.identity()));
        List<Row> rows=all.stream().map(s->row(s,gameById.get(s.getGameId()),teamById)).toList(); int from=Math.min(page*size,rows.size()),to=Math.min(from+size,rows.size());return new Page(page,size,rows.size(),rows.subList(from,to));
    }
    private Row row(GoalieGameStats s,Game game,Map<Integer,Team> teams){ if(game==null) throw new IllegalStateException("Missing game for goalie stat line."); boolean home=s.getTeamId().equals(game.getHomeTeamId()); Integer opponentId=home?game.getAwayTeamId():game.getHomeTeamId(); Team team=teams.get(s.getTeamId()), opponent=teams.get(opponentId); Integer own=home?game.getHomeScore():game.getAwayScore(), opp=home?game.getAwayScore():game.getHomeScore(); return new Row(s.getGameId(),game.getGameDate(),team==null?null:team.getAbbreviation(),opponent==null?null:opponent.getAbbreviation(),own,opp,s.getDecision(),s.getIsStarter(),s.getTimeOnIceSeconds(),s.getShotsAgainst(),s.getSaves(),s.getGoalsAgainst(),s.getSavePctg()); }
    public record Page(int page,int size,int total,List<Row> rows){} public record Row(Long gameId,java.time.LocalDate gameDate,String team,String opponent,Integer teamScore,Integer opponentScore,String decision,Boolean starter,Integer timeOnIceSeconds,Integer shotsAgainst,Integer saves,Integer goalsAgainst,Double savePctg){}
}
