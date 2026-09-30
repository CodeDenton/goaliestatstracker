package com.creasevision.api.ingestion;

import java.util.List;

import com.creasevision.api.model.Goalie;

/**
 * Boundary around NHL's public API. Keeping network calls here makes the
 * import workflow deterministic and easy to test without calling NHL.
 */
public interface NhlGoalieClient {

    List<Goalie> fetchGoalies(String season, String situation);
}
