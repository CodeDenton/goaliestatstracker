package com.creasevision.api.service;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.GoalieSeasonStats;
import com.creasevision.api.model.GoalieTeamStint;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.repository.GoalieSeasonStatsRepository;
import com.creasevision.api.repository.GoalieTeamStintRepository;
@Service public class GoalieCompareService {
    private final GoalieRepository goalies; private final GoalieTeamStintRepository stints; private final GoalieSeasonStatsRepository stats;
    public GoalieCompareService(GoalieRepository goalies,GoalieTeamStintRepository stints,GoalieSeasonStatsRepository stats){this.goalies=goalies;this.stints=stints;this.stats=stats;}
    @Cacheable("goalie-compare") @Transactional(readOnly=true) public Result compare(String season,List<Long> ids){if(season==null||!season.matches("\\d{8}")||ids==null||ids.size()<2||ids.size()>4||ids.stream().distinct().count()!=ids.size())throw new IllegalArgumentException("Compare exactly two to four distinct goalies for a valid season.");return new Result(season,ids.stream().map(id->row(season,id)).toList());}
    private Row row(String season,Long id){Goalie g=goalies.findById(id).orElseThrow(()->new NotFoundException("Goalie "+id+" was not found.")); List<Long> stintIds=stints.findByGoalieIdAndSeasonId(id,season).stream().map(GoalieTeamStint::getId).toList(); List<GoalieSeasonStats> lines=stats.findByTeamStintIdIn(stintIds); int gp=lines.stream().mapToInt(l->n(l.getGamesPlayed())).sum(),w=lines.stream().mapToInt(l->n(l.getWins())).sum(),l=lines.stream().mapToInt(x->n(x.getLosses())).sum(),ot=lines.stream().mapToInt(x->n(x.getOvertimeLosses())).sum(),s=lines.stream().mapToInt(x->n(x.getSaves())).sum(),sa=lines.stream().mapToInt(x->n(x.getShotsAgainst())).sum();return new Row(id,(g.getFirstName()+" "+g.getLastName()).trim(),gp,w,l,ot,s,sa,sa==0?0:(double)s/sa);}
    private int n(Integer n){return n==null?0:n;} public record Result(String season,List<Row> goalies){} public record Row(Long goalieId,String name,int gamesPlayed,int wins,int losses,int overtimeLosses,int saves,int shotsAgainst,double savePctg){}
}
