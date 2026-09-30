package com.creasevision.api.ingestion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.ShotLocationDetail;
import com.creasevision.api.model.ShotLocationSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Deliberate, retrying NHL API reader that rejects non-JSON error pages. */
@Component
public class NhlHttpGoalieClient implements NhlGoalieClient {

    private static final Logger log = LoggerFactory.getLogger(NhlHttpGoalieClient.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final List<String> TEAMS = List.of(
            "ANA", "BOS", "BUF", "CAR", "CBJ", "CGY", "CHI", "COL", "DAL", "DET",
            "EDM", "FLA", "LAK", "MIN", "MTL", "NJD", "NSH", "NYI", "NYR", "OTT",
            "PHI", "PIT", "SEA", "SJS", "STL", "TBL", "TOR", "UTA", "VAN", "VGK",
            "WPG", "WSH");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public NhlHttpGoalieClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL).build(), new ObjectMapper());
    }

    NhlHttpGoalieClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Goalie> fetchGoalies(String season, String situation) {
        Set<Long> goalieIds = new LinkedHashSet<>();
        int successfulRosters = 0;
        for (String team : TEAMS) {
            try {
                JsonNode roster = getJson("https://api-web.nhle.com/v1/roster/" + team + "/" + season);
                successfulRosters++;
                for (JsonNode goalie : roster.path("goalies")) {
                    long id = goalie.path("id").asLong();
                    if (id > 0) goalieIds.add(id);
                }
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("NHL roster import was interrupted", exception);
                }
                log.warn("Could not fetch {} roster for {}: {}", team, season, exception.getMessage());
            }
        }
        if (successfulRosters == 0) throw new IllegalStateException("No NHL rosters could be fetched for " + season);
        log.info("Found {} unique goalies across {} NHL rosters for {}.", goalieIds.size(), successfulRosters, season);

        List<Goalie> goalies = new ArrayList<>();
        for (Long goalieId : goalieIds) {
            try {
                Goalie goalie = toGoalie(getJson("https://api-web.nhle.com/v1/edge/goalie-detail/"
                        + goalieId + "/" + season + "/" + situation));
                if (goalie != null) goalies.add(goalie);
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("NHL goalie-detail import was interrupted", exception);
                }
                log.warn("Could not fetch NHL details for goalie {} in {}: {}", goalieId, season, exception.getMessage());
            }
        }
        if (!goalieIds.isEmpty() && goalies.isEmpty()) {
            throw new IllegalStateException("NHL returned no usable goalie details for " + season);
        }
        return goalies;
    }

    private JsonNode getJson(String url) throws IOException, InterruptedException {
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30))
                        .header("Accept", "application/json").header("User-Agent", "CreaseVision/1.0").GET().build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                String contentType = response.headers().firstValue("content-type").orElse("");
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("NHL returned HTTP " + response.statusCode());
                }
                if (!contentType.toLowerCase().contains("application/json")) {
                    throw new IOException("NHL returned " + (contentType.isBlank() ? "an unknown content type" : contentType));
                }
                return objectMapper.readTree(response.body());
            } catch (JsonProcessingException exception) {
                lastFailure = new IOException("NHL returned invalid JSON", exception);
            } catch (IOException exception) {
                lastFailure = exception;
            }
            if (attempt < MAX_ATTEMPTS) Thread.sleep(Duration.ofMillis(500L * attempt));
        }
        throw lastFailure;
    }

    private Goalie toGoalie(JsonNode node) {
        JsonNode player = node.path("player");
        long id = player.path("id").asLong();
        if (id == 0) return null;
        Goalie goalie = new Goalie();
        goalie.setId(id);
        goalie.setFirstName(player.path("firstName").path("default").asText());
        goalie.setLastName(player.path("lastName").path("default").asText());
        goalie.setHeadshot(player.path("headshot").asText());
        goalie.setSweaterNumber(player.path("sweaterNumber").asInt());
        goalie.setTeamAbbrev(player.path("team").path("abbrev").asText());
        goalie.setTeamName(player.path("team").path("commonName").path("default").asText());
        goalie.setTeamLogoLight(player.path("team").path("teamLogo").path("light").asText());
        goalie.setTeamLogoDark(player.path("team").path("teamLogo").path("dark").asText());
        goalie.setWins(player.path("wins").asInt());
        goalie.setLosses(player.path("losses").asInt());
        goalie.setOvertimeLosses(player.path("overtimeLosses").asInt());
        goalie.setGamesPlayed(player.path("gamesPlayed").asInt());
        goalie.setGoalsAgainstAvg(player.path("goalsAgainstAvg").asDouble());
        goalie.setSavePctg(player.path("savePctg").asDouble());
        goalie.setShotLocationSummary(toSummaries(node.path("shotLocationSummary"), goalie));
        goalie.setShotLocationDetails(toDetails(node.path("shotLocationDetails"), goalie));
        return goalie;
    }

    private List<ShotLocationSummary> toSummaries(JsonNode source, Goalie goalie) {
        List<ShotLocationSummary> summaries = new ArrayList<>();
        for (JsonNode value : source) {
            ShotLocationSummary summary = new ShotLocationSummary();
            summary.setGoalie(goalie);
            summary.setLocationCode(value.path("locationCode").asText());
            summary.setGoalsAgainst(value.path("goalsAgainst").asInt());
            summary.setGoalsAgainstPercentile(value.path("goalsAgainstPercentile").asDouble());
            summary.setGoalsAgainstLeagueAvg(value.path("goalsAgainstLeagueAvg").asDouble());
            summary.setSaves(value.path("saves").asInt());
            summary.setSavesPercentile(value.path("savesPercentile").asDouble());
            summary.setSavesLeagueAvg(value.path("savesLeagueAvg").asDouble());
            summary.setSavePctg(value.path("savePctg").asDouble());
            summary.setSavePctgPercentile(value.path("savePctgPercentile").asDouble());
            summary.setSavePctgLeagueAvg(value.path("savePctgLeagueAvg").asDouble());
            summaries.add(summary);
        }
        return summaries;
    }

    private List<ShotLocationDetail> toDetails(JsonNode source, Goalie goalie) {
        List<ShotLocationDetail> details = new ArrayList<>();
        for (JsonNode value : source) {
            ShotLocationDetail detail = new ShotLocationDetail();
            detail.setGoalie(goalie);
            detail.setArea(value.path("area").asText());
            detail.setSaves(value.path("saves").asInt());
            detail.setSavesPercentile(value.path("savesPercentile").asDouble());
            detail.setSavePctg(value.path("savePctg").asDouble());
            detail.setSavePctgPercentile(value.path("savePctgPercentile").asDouble());
            details.add(detail);
        }
        return details;
    }
}
