package com.creasevision.api.service;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creasevision.api.dto.ShotMapDTO;
import com.creasevision.api.model.Game;
import com.creasevision.api.model.GoalieGameStats;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.GoalieGameStatsRepository;
@Service public class GameDetailService {
    private final GameRepository games; private final GoalieGameStatsRepository stats; private final ShotMapService maps;
    public GameDetailService(GameRepository games,GoalieGameStatsRepository stats,ShotMapService maps){this.games=games;this.stats=stats;this.maps=maps;}
    @Transactional(readOnly=true) public Detail game(Long id){Game game=games.findById(id).orElseThrow(()->new NotFoundException("Game "+id+" was not found.")); List<GoalieLine> lines=stats.findByGameId(id).stream().map(s->new GoalieLine(s.getGoalieId(),s.getTeamId(),s.getDecision(),s.getShotsAgainst(),s.getSaves(),s.getGoalsAgainst(),s.getSavePctg(),maps.goalieMap(s.getGoalieId(),game.getSeasonId(),id))).toList();return new Detail(game,lines);}
    public record Detail(Game game,List<GoalieLine> goalieLines){} public record GoalieLine(Long goalieId,Integer teamId,String decision,Integer shotsAgainst,Integer saves,Integer goalsAgainst,Double savePctg,ShotMapDTO shotMap){}
}
