package com.creasevision.api.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creasevision.api.model.*;
import com.creasevision.api.repository.*;

@Service
public class GoalieProfileService {
 private final GoalieRepository goalies; private final GoalieTeamStintRepository stints; private final GoalieSeasonStatsRepository stats;
 public GoalieProfileService(GoalieRepository goalies,GoalieTeamStintRepository stints,GoalieSeasonStatsRepository stats){this.goalies=goalies;this.stints=stints;this.stats=stats;}
 @Transactional(readOnly=true) public Profile profile(Long goalieId){Goalie goalie=goalies.findById(goalieId).orElseThrow(()->new IllegalArgumentException("Goalie not found."));List<GoalieTeamStint> lines=stints.findByGoalieIdOrderBySeasonIdDesc(goalieId);Map<Long,GoalieSeasonStats> byStint=stats.findByTeamStintIdIn(lines.stream().map(GoalieTeamStint::getId).toList()).stream().collect(Collectors.toMap(GoalieSeasonStats::getTeamStintId,value->value));return new Profile(goalieId,(goalie.getFirstName()+" "+goalie.getLastName()).trim(),lines.stream().map(stint->season(stint,byStint.get(stint.getId()))).toList());}
 private SeasonLine season(GoalieTeamStint stint,GoalieSeasonStats line){int saves=line==null||line.getSaves()==null?0:line.getSaves();int shots=line==null||line.getShotsAgainst()==null?0:line.getShotsAgainst();return new SeasonLine(stint.getSeasonId(),line==null?0:line.getGamesPlayed(),line==null?0:line.getWins(),saves,shots,shots==0?0:(double)saves/shots);}
 public record Profile(Long goalieId,String name,List<SeasonLine> seasons){} public record SeasonLine(String seasonId,int gamesPlayed,int wins,int saves,int shotsAgainst,double savePctg){}
}
