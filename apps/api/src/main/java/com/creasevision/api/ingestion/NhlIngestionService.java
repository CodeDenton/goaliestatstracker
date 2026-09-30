package com.creasevision.api.ingestion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.repository.GoalieRepository;

@Service
public class NhlIngestionService {

    private static final Logger log = LoggerFactory.getLogger(NhlIngestionService.class);

    private final NhlGoalieClient nhlGoalieClient;
    private final GoalieRepository goalieRepository;
    private final NhlImportProperties properties;

    public NhlIngestionService(
            NhlGoalieClient nhlGoalieClient,
            GoalieRepository goalieRepository,
            NhlImportProperties properties) {
        this.nhlGoalieClient = nhlGoalieClient;
        this.goalieRepository = goalieRepository;
        this.properties = properties;
    }

    public ImportResult refreshCurrentSeason() {
        try {
            List<Goalie> goalies = nhlGoalieClient.fetchGoalies(
                    properties.getSeason(), properties.getSituation());

            if (goalies.isEmpty()) {
                log.warn("NHL import returned no goalies for season {}; leaving existing data untouched.",
                        properties.getSeason());
                return ImportResult.noData(properties.getSeason());
            }

            goalieRepository.saveAll(goalies);
            log.info("Saved {} goalies for season {}.", goalies.size(), properties.getSeason());
            return ImportResult.success(properties.getSeason(), goalies.size());
        } catch (RuntimeException exception) {
            log.error("NHL import failed for season {}.", properties.getSeason(), exception);
            return ImportResult.failure(properties.getSeason(), exception.getMessage());
        }
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
    }

    public enum Status {
        SUCCESS,
        NO_DATA,
        FAILED
    }
}
