package com.creasevision.api.ingestion;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.repository.GoalieRepository;
import com.creasevision.api.model.ImportRun;
import com.creasevision.api.model.Season;
import com.creasevision.api.repository.ImportRunRepository;
import com.creasevision.api.repository.SeasonRepository;

@Service
public class NhlIngestionService {

    private static final Logger log = LoggerFactory.getLogger(NhlIngestionService.class);

    private final NhlGoalieClient nhlGoalieClient;
    private final GoalieRepository goalieRepository;
    private final NhlImportProperties properties;
    private final ImportRunRepository importRuns;
    private final SeasonRepository seasons;
    private final AtomicBoolean refreshRunning = new AtomicBoolean(false);

    public NhlIngestionService(
            NhlGoalieClient nhlGoalieClient,
            GoalieRepository goalieRepository,
            NhlImportProperties properties,
            ImportRunRepository importRuns,
            SeasonRepository seasons) {
        this.nhlGoalieClient = nhlGoalieClient;
        this.goalieRepository = goalieRepository;
        this.properties = properties;
        this.importRuns = importRuns;
        this.seasons = seasons;
    }

    public ImportResult refreshCurrentSeason() {
        if (!refreshRunning.compareAndSet(false, true)) {
            return ImportResult.inProgress(properties.getSeason());
        }

        ensureCurrentSeason();
        ImportRun run = new ImportRun();
        run.setImportType("CURRENT_SEASON_REFRESH");
        run.setSeasonId(properties.getSeason());
        run.setStartedAt(OffsetDateTime.now());
        run.setStatus("RUNNING");
        run = importRuns.save(run);
        try {
            List<Goalie> goalies = nhlGoalieClient.fetchGoalies(
                    properties.getSeason(), properties.getSituation());

            if (goalies.isEmpty()) {
                log.warn("NHL import returned no goalies for season {}; leaving existing data untouched.",
                        properties.getSeason());
                return complete(run, ImportResult.noData(properties.getSeason()));
            }

            goalieRepository.saveAll(goalies);
            log.info("Saved {} goalies for season {}.", goalies.size(), properties.getSeason());
            return complete(run, ImportResult.success(properties.getSeason(), goalies.size()));
        } catch (RuntimeException exception) {
            log.error("NHL import failed for season {}.", properties.getSeason(), exception);
            return complete(run, ImportResult.failure(properties.getSeason(), exception.getMessage()));
        } finally {
            refreshRunning.set(false);
        }
    }

    private ImportResult complete(ImportRun run, ImportResult result) {
        run.setCompletedAt(OffsetDateTime.now());
        run.setRecordsRead(result.savedGoalies());
        run.setRecordsWritten(result.savedGoalies());
        run.setStatus(switch (result.status()) {
            case SUCCESS -> "SUCCEEDED";
            case NO_DATA -> "PARTIAL";
            case FAILED -> "FAILED";
            case IN_PROGRESS -> throw new IllegalArgumentException("An in-progress refresh cannot be completed.");
        });
        run.setErrorDetail(result.detail());
        importRuns.save(run);
        return result;
    }

    private void ensureCurrentSeason() {
        String seasonId = properties.getSeason();
        if (!seasonId.matches("\\d{8}")) {
            throw new IllegalArgumentException("Season must use the NHL format YYYYYYYY.");
        }

        int startYear = Integer.parseInt(seasonId.substring(0, 4));
        int endYear = Integer.parseInt(seasonId.substring(4));
        if (endYear != startYear + 1) {
            throw new IllegalArgumentException("Season years must be consecutive.");
        }
        if (seasons.existsById(seasonId)) {
            return;
        }

        Season season = new Season();
        season.setId(seasonId);
        season.setDisplayName(startYear + "-" + String.format("%02d", endYear % 100));
        season.setStartsOn(LocalDate.of(startYear, 10, 1));
        season.setEndsOn(LocalDate.of(endYear, 6, 30));
        season.setCurrent(true);
        seasons.save(season);
    }

    public record ImportResult(String season, int savedGoalies, Status status, String detail) {

        public static ImportResult success(String season, int savedGoalies) {
            return new ImportResult(season, savedGoalies, Status.SUCCESS, null);
        }

        public static ImportResult noData(String season) {
            return new ImportResult(season, 0, Status.NO_DATA, "The NHL API returned no goalies.");
        }

        public static ImportResult failure(String season, String detail) {
            return new ImportResult(season, 0, Status.FAILED, detail);
        }

        public static ImportResult inProgress(String season) {
            return new ImportResult(season, 0, Status.IN_PROGRESS, "A refresh is already running.");
        }
    }

    public enum Status {
        SUCCESS,
        NO_DATA,
        FAILED,
        IN_PROGRESS
    }
}
