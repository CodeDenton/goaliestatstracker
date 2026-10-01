package com.creasevision.api.ingestion;
import java.util.List;
import org.springframework.stereotype.Service;
@Service public class SeasonGameImportService {
 private final NhlGoalieClient nhl; private final GameImportService games;
 public SeasonGameImportService(NhlGoalieClient nhl,GameImportService games){this.nhl=nhl;this.games=games;}
 public Result importSeason(String season){if(season==null||!season.matches("\\d{8}"))throw new IllegalArgumentException("Season must use YYYYYYYY format.");List<Long> ids=nhl.fetchRegularSeasonGameIds(season);int lines=0,failed=0;for(Long id:ids)try{lines+=games.importGame(id);}catch(RuntimeException ignored){failed++;}return new Result(season,ids.size(),lines,failed);}
 public record Result(String season,int gamesRead,int goalieLinesWritten,int failedGames){}
}
