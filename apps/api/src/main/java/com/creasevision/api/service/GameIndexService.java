package com.creasevision.api.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.creasevision.api.model.Game;
import com.creasevision.api.repository.GameRepository;
import com.creasevision.api.repository.TeamRepository;

@Service
public class GameIndexService {
    private final GameRepository games; private final TeamRepository teams;
    public GameIndexService(GameRepository games, TeamRepository teams) { this.games=games; this.teams=teams; }
    @Cacheable("game-index") @Transactional(readOnly=true)
    public Page games(String season, String team, int page, int size) {
        if (season == null || !season.matches("\\d{8}") || page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid game-index request.");
        List<Row> rows=games.findBySeasonIdAndGameTypeOrderByGameDateDescIdDesc(season,(short)2).stream()
                .filter(g -> team == null || team.equalsIgnoreCase(abbreviation(g.getHomeTeamId())) || team.equalsIgnoreCase(abbreviation(g.getAwayTeamId())))
                .map(g -> new Row(g.getId(),g.getGameDate(),abbreviation(g.getAwayTeamId()),abbreviation(g.getHomeTeamId()),g.getAwayScore(),g.getHomeScore(),g.getGameState())) .toList();
        int from=Math.min(page*size,rows.size()),to=Math.min(from+size,rows.size()); return new Page(season,page,size,rows.size(),rows.subList(from,to));
    }
    private String abbreviation(Integer id) { return teams.findById(id).map(t->t.getAbbreviation()).orElse(null); }
    public record Page(String season,int page,int size,int total,List<Row> rows){} public record Row(Long gameId,LocalDate gameDate,String awayTeam,String homeTeam,Integer awayScore,Integer homeScore,String gameState){}
}
