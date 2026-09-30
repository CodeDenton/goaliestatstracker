package com.creasevision.api.ingestion;

import java.util.List;

import com.creasevision.api.model.Goalie;

/**
 * Boundary around NHL's public API. Keeping network calls here makes the
 * import workflow deterministic and easy to test without calling NHL.
 */
public interface NhlGoalieClient {

    List<Goalie> fetchGoalies(String season, String situation);

    /**
     * Returns one row for every goalie/team combination in the regular season.
     * Unlike {@link #fetchGoalies(String, String)}, these rows must never be
     * aggregate or playoff lines.
     */
    List<NhlGoalieSeasonRow> fetchRegularSeasonTeamSplits(String season);

    List<NhlGoalieGameRow> fetchGoalieGameRows(long gameId);

    List<NhlShotZoneRow> fetchShotZoneRows(long gameId);
}
