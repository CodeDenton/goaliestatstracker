package com.creasevision.api.ingestion;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.creasevision.api.model.*;
import com.creasevision.api.repository.*;

@Service
public class SeasonImportService {
    private final NhlGoalieClient nhl; private final NhlImportProperties config; private final GoalieProfileStore profiles;
    private final SeasonRepository seasons; private final TeamRepository teams; private final GoalieTeamStintRepository stints;
    private final GoalieSeasonStatsRepository stats; private final ImportRunRepository runs;
    public SeasonImportService(NhlGoalieClient nhl, NhlImportProperties config, GoalieProfileStore profiles, SeasonRepository seasons,
            TeamRepository teams, GoalieTeamStintRepository stints, GoalieSeasonStatsRepository stats, ImportRunRepository runs) {
        this.nhl=nhl; this.config=config; this.profiles=profiles; this.seasons=seasons; this.teams=teams; this.stints=stints; this.stats=stats; this.runs=runs;
    }
    public SeasonImportResult importSeason(String id) {
        ensureSeason(id); ImportRun run = new ImportRun(); run.setImportType("GOALIE_SEASON_STATS"); run.setSeasonId(id); run.setStartedAt(OffsetDateTime.now()); run.setStatus("RUNNING"); run=runs.save(run);
        try {
            List<Goalie> imported=nhl.fetchGoalies(id, config.getSituation());
            if(imported.isEmpty()) return complete(run,"FAILED",0,0,"The NHL API returned no goalies.");
            int writes=0; for(Goalie goalie:imported) writes+=save(goalie,id);
            return complete(run,"SUCCEEDED",imported.size(),writes,null);
        } catch(RuntimeException e) { return complete(run,"FAILED",0,0,e.getMessage()); }
    }
    private int save(Goalie goalie,String seasonId) {
        if(goalie.getId()==null||goalie.getTeamAbbrev()==null||goalie.getTeamAbbrev().isBlank()) return 0;
        profiles.saveProfile(goalie);
        Team team=teams.findByAbbreviation(goalie.getTeamAbbrev()).orElseGet(Team::new);
        team.setAbbreviation(goalie.getTeamAbbrev()); team.setCommonName(goalie.getTeamName()); team.setLogoLightUrl(goalie.getTeamLogoLight()); team.setLogoDarkUrl(goalie.getTeamLogoDark()); team=teams.save(team);
        GoalieTeamStint stint=stints.findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(goalie.getId(),seasonId,team.getId(),(short)1).orElseGet(GoalieTeamStint::new);
        stint.setGoalieId(goalie.getId()); stint.setSeasonId(seasonId); stint.setTeamId(team.getId()); stint.setStintNumber((short)1); stint.setSweaterNumber(goalie.getSweaterNumber()); stint.setSourceName("NHL API"); stint.setSourceUpdatedAt(OffsetDateTime.now()); stint=stints.save(stint);
        GoalieSeasonStats line=stats.findByTeamStintId(stint.getId()).orElseGet(GoalieSeasonStats::new);
        line.setTeamStintId(stint.getId()); line.setGamesPlayed(zero(goalie.getGamesPlayed())); line.setWins(zero(goalie.getWins())); line.setLosses(zero(goalie.getLosses())); line.setOvertimeLosses(zero(goalie.getOvertimeLosses())); line.setGoalsAgainstAvg(goalie.getGoalsAgainstAvg()); line.setSavePctg(goalie.getSavePctg()); line.setSourceName("NHL API"); line.setSourceUpdatedAt(OffsetDateTime.now()); stats.save(line); return 1;
    }
    private void ensureSeason(String id) {
        if(!id.matches("\\d{8}")) throw new IllegalArgumentException("Season must use the NHL format YYYYYYYY."); int start=Integer.parseInt(id.substring(0,4)); int end=Integer.parseInt(id.substring(4)); if(end!=start+1) throw new IllegalArgumentException("Season years must be consecutive.");
        if(seasons.existsById(id)) return; Season season=new Season(); season.setId(id); season.setDisplayName(start+"-"+String.format("%02d",end%100)); season.setStartsOn(LocalDate.of(start,10,1)); season.setEndsOn(LocalDate.of(end,6,30)); season.setCurrent(id.equals(config.getSeason())); seasons.save(season);
    }
    private SeasonImportResult complete(ImportRun run,String status,int read,int written,String error) { run.setStatus(status);run.setRecordsRead(read);run.setRecordsWritten(written);run.setErrorDetail(error);run.setCompletedAt(OffsetDateTime.now());runs.save(run);return new SeasonImportResult(run.getSeasonId(),status,read,written,error); }
    private int zero(Integer value){return value==null?0:value;}
    public record SeasonImportResult(String seasonId,String status,int recordsRead,int recordsWritten,String errorDetail){}
}
