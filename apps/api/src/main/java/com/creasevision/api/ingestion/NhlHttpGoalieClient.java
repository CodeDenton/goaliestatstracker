package com.creasevision.api.ingestion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<Long, JsonNode> rosterGoalies = new LinkedHashMap<>();
        int successfulRosters = 0;
        for (String team : TEAMS) {
            try {
                JsonNode roster = getJson("https://api-web.nhle.com/v1/roster/" + team + "/" + season);
                successfulRosters++;
                for (JsonNode goalie : roster.path("goalies")) {
                    long id = goalie.path("id").asLong();
                    if (id > 0) rosterGoalies.put(id, goalie);
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
        log.info("Found {} unique goalies across {} NHL rosters for {}.", rosterGoalies.size(), successfulRosters, season);

        List<Goalie> goalies = new ArrayList<>();
        for (Map.Entry<Long, JsonNode> rosterGoalie : rosterGoalies.entrySet()) {
            Long goalieId = rosterGoalie.getKey();
            try {
                Goalie goalie = toGoalie(getJson("https://api-web.nhle.com/v1/edge/goalie-detail/"
                        + goalieId + "/" + season + "/" + situation), rosterGoalie.getValue());
                if (goalie != null) goalies.add(goalie);
            } catch (IOException | InterruptedException exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("NHL goalie-detail import was interrupted", exception);
                }
                log.warn("Could not fetch NHL details for goalie {} in {}: {}", goalieId, season, exception.getMessage());
            }
        }
        if (!rosterGoalies.isEmpty() && goalies.isEmpty()) {
            throw new IllegalStateException("NHL returned no usable goalie details for " + season);
        }
        return goalies;
    }

    @Override
    public List<NhlGoalieSeasonRow> fetchRegularSeasonTeamSplits(String season) {
        try {
            List<NhlGoalieSeasonRow> rows = new ArrayList<>();
            for (JsonNode team : getJson("https://api.nhle.com/stats/rest/en/team").path("data")) {
                int teamId = team.path("id").asInt();
                String abbreviation = firstText(team, "triCode", "rawTricode");
                if (teamId <= 0 || abbreviation == null) continue;
                JsonNode response = getJson("https://api.nhle.com/stats/rest/en/goalie/summary?limit=10000"
                        + "&cayenneExp=seasonId%3D" + season + "%20and%20gameTypeId%3D2%20and%20teamId%3D" + teamId);
                rows.addAll(toRegularSeasonTeamSplits(response, abbreviation, firstText(team, "fullName")));
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not fetch NHL goalie team splits for " + season, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("NHL goalie team-split import was interrupted", exception);
        }
    }

    @Override
    public List<NhlGoalieGameRow> fetchGoalieGameRows(long gameId) {
        try {
            return toGoalieGameRows(getJson("https://api-web.nhle.com/v1/gamecenter/" + gameId + "/boxscore"));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not fetch NHL game " + gameId, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("NHL game import was interrupted", exception);
        }
    }

    @Override public List<NhlShotZoneRow> fetchShotZoneRows(long gameId) {
        try { return toShotZoneRows(getJson("https://api-web.nhle.com/v1/gamecenter/" + gameId + "/play-by-play")); }
        catch (IOException e) { throw new IllegalStateException("Could not fetch NHL play-by-play for " + gameId,e); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("NHL play-by-play import was interrupted",e); }
    }

    @Override
    public List<NhlShotEventRow> fetchShotEvents(long gameId) {
        try {
            return toShotEvents(getJson("https://api-web.nhle.com/v1/gamecenter/" + gameId + "/play-by-play"));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not fetch NHL play-by-play for " + gameId, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("NHL play-by-play import was interrupted", exception);
        }
    }

    @Override
    public List<Long> fetchRegularSeasonGameIds(String season) {
        try {
            java.util.Set<Long> ids = new java.util.TreeSet<>();
            for (String team : TEAMS) for (JsonNode game : getJson("https://api-web.nhle.com/v1/club-schedule-season/" + team + "/" + season).path("games")) {
                long id = game.path("id").asLong();
                if (id > 0 && game.path("gameType").asInt() == 2) ids.add(id);
            }
            return List.copyOf(ids);
        } catch (IOException exception) { throw new IllegalStateException("Could not fetch NHL regular-season schedule for " + season, exception); }
        catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new IllegalStateException("NHL schedule import was interrupted", exception); }
    }

    List<NhlShotEventRow> toShotEvents(JsonNode game) {
        List<NhlShotEventRow> events = new ArrayList<>();
        long gameId = game.path("id").asLong();
        String seasonId = game.path("season").asText();
        int homeTeamId = game.path("homeTeam").path("id").asInt();
        String homeTeam = game.path("homeTeam").path("abbrev").asText();
        String awayTeam = game.path("awayTeam").path("abbrev").asText();

        for (JsonNode play : game.path("plays")) {
            String type = play.path("typeDescKey").asText();
            if (!type.equals("shot-on-goal") && !type.equals("goal")) continue;
            JsonNode details = play.path("details");
            long goalieId = details.path("goalieInNetId").asLong();
            int eventId = play.path("eventId").asInt();
            if (goalieId <= 0 || eventId <= 0 || !details.hasNonNull("xCoord") || !details.hasNonNull("yCoord")) continue;
            String defendingTeam = details.path("eventOwnerTeamId").asInt() == homeTeamId ? awayTeam : homeTeam;
            events.add(new NhlShotEventRow(
                    gameId, seasonId, eventId, goalieId, defendingTeam,
                    details.path("xCoord").asInt(), details.path("yCoord").asInt(), type.equals("goal")));
        }
        return events;
    }
    List<NhlShotZoneRow> toShotZoneRows(JsonNode game) {
        Map<String,int[]> totals=new LinkedHashMap<>(); long id=game.path("id").asLong(); String season=game.path("season").asText(); int homeId=game.path("homeTeam").path("id").asInt(); String home=game.path("homeTeam").path("abbrev").asText(); String away=game.path("awayTeam").path("abbrev").asText();
        for(JsonNode play:game.path("plays")) { String type=play.path("typeDescKey").asText(); if(!type.equals("shot-on-goal")&&!type.equals("goal"))continue; JsonNode d=play.path("details"); long goalie=d.path("goalieInNetId").asLong(); if(goalie<=0)continue; String team=d.path("eventOwnerTeamId").asInt()==homeId?away:home; String zone=zone(d.path("xCoord").asInt(),d.path("yCoord").asInt()); String key=goalie+"|"+team+"|"+zone; int[] count=totals.computeIfAbsent(key,k->new int[3]); count[0]++; if(type.equals("goal"))count[2]++;else count[1]++; }
        List<NhlShotZoneRow> rows=new ArrayList<>(); totals.forEach((key,count)->{String[] parts=key.split("\\|");rows.add(new NhlShotZoneRow(id,season,Long.valueOf(parts[0]),parts[1],parts[2],count[0],count[1],count[2]));}); return rows;
    }
    private String zone(int x,int y) { int distance=(int)Math.hypot(89-Math.abs(x),y); return distance<=20?"INNER_SLOT":distance<=40?"SLOT":"PERIMETER"; }

    List<NhlGoalieGameRow> toGoalieGameRows(JsonNode game) {
        List<NhlGoalieGameRow> rows = new ArrayList<>();
        long gameId = game.path("id").asLong();
        String season = game.path("season").asText();
        String home = game.path("homeTeam").path("abbrev").asText();
        String away = game.path("awayTeam").path("abbrev").asText();
        for (String side : List.of("homeTeam", "awayTeam")) {
            String team = side.equals("homeTeam") ? home : away;
            for (JsonNode goalie : game.path("playerByGameStats").path(side).path("goalies")) {
                long goalieId = goalie.path("playerId").asLong();
                if (goalieId <= 0 || team.isBlank()) continue;
                int shots = integerOrNull(goalie, "shotsAgainst") == null ? shotsAgainst(goalie) : integerOrNull(goalie, "shotsAgainst");
                int saves = integerOrNull(goalie, "saves") == null ? savesFromPair(goalie) : integerOrNull(goalie, "saves");
                Integer goals = integerOrNull(goalie, "goalsAgainst");
                rows.add(new NhlGoalieGameRow(gameId, season, LocalDate.parse(game.path("gameDate").asText()), (short) game.path("gameType").asInt(), home, away,
                        integerOrNull(game.path("homeTeam"), "score"), integerOrNull(game.path("awayTeam"), "score"), game.path("gameState").asText(null),
                        goalieId, team, goalie.path("starter").asBoolean(false), goalie.path("decision").asText(null), toiSeconds(goalie.path("toi").asText()), shots, saves, goals,
                        shots == 0 ? null : (double) saves / shots));
            }
        }
        return rows;
    }

    private int shotsAgainst(JsonNode goalie) { String[] value = goalie.path("saveShotsAgainst").asText("0/0").split("/"); return value.length == 2 ? Integer.parseInt(value[1]) : 0; }
    private int savesFromPair(JsonNode goalie) { String[] value = goalie.path("saveShotsAgainst").asText("0/0").split("/"); return value.length == 2 ? Integer.parseInt(value[0]) : 0; }
    private Integer toiSeconds(String value) { if (value == null || !value.matches("\\d+:\\d{2}")) return null; String[] parts=value.split(":"); return Integer.parseInt(parts[0])*60+Integer.parseInt(parts[1]); }

    /** Maps only regular-season, single-team lines; aggregate and playoff rows are not importable stints. */
    List<NhlGoalieSeasonRow> toRegularSeasonTeamSplits(JsonNode response) {
        return toRegularSeasonTeamSplits(response, null, null);
    }

    private List<NhlGoalieSeasonRow> toRegularSeasonTeamSplits(JsonNode response, String requestedTeam, String requestedTeamName) {
        List<NhlGoalieSeasonRow> rows = new ArrayList<>();
        for (JsonNode value : response.path("data")) {
            if (value.path("gameTypeId").asInt() != 2) continue;
            String team = requestedTeam == null ? firstText(value, "teamAbbrevs", "teamAbbrev") : requestedTeam;
            if (team == null || team.contains(",")) continue;
            long goalieId = value.path("playerId").asLong();
            if (goalieId <= 0) continue;
            String fullName = firstText(value, "goalieFullName", "playerName");
            String[] names = fullName == null ? new String[] { null, null } : fullName.trim().split("\\s+", 2);
            rows.add(new NhlGoalieSeasonRow(
                    goalieId,
                    names.length > 0 ? names[0] : null,
                    names.length > 1 ? names[1] : null,
                    team,
                    requestedTeamName == null ? (firstText(value, "teamName") == null ? team : firstText(value, "teamName")) : requestedTeamName,
                    integerOrNull(value, "gamesPlayed"),
                    integerOrNull(value, "wins"),
                    integerOrNull(value, "losses"),
                    integerOrNull(value, "otLosses", "overtimeLosses"),
                    integerOrNull(value, "saves"),
                    integerOrNull(value, "shotsAgainst"),
                    doubleOrNull(value, "goalsAgainstAverage", "goalsAgainstAvg"),
                    doubleOrNull(value, "savePct", "savePctg")));
        }
        return rows;
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

    Goalie toGoalie(JsonNode node, JsonNode rosterGoalie) {
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
        applyStableBiography(goalie, rosterGoalie);
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

    private void applyStableBiography(Goalie goalie, JsonNode rosterGoalie) {
        if (rosterGoalie == null || rosterGoalie.isMissingNode()) return;

        goalie.setHeightCm(integerOrNull(rosterGoalie, "heightInCentimeters"));
        goalie.setWeightKg(integerOrNull(rosterGoalie, "weightInKilograms"));
        goalie.setCatches(textOrNull(rosterGoalie, "shootsCatches"));
        goalie.setBirthDate(localDateOrNull(rosterGoalie, "birthDate"));
        goalie.setBirthCity(rosterGoalie.path("birthCity").path("default").asText(null));
        goalie.setBirthStateProvince(rosterGoalie.path("birthStateProvince").path("default").asText(null));
        goalie.setBirthCountry(textOrNull(rosterGoalie, "birthCountry"));
        goalie.setPositionCode(textOrNull(rosterGoalie, "positionCode"));
        goalie.setProfileUpdatedAt(OffsetDateTime.now());
    }

    private Integer integerOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asInt() : null;
    }

    private Integer integerOrNull(JsonNode node, String... fields) {
        for (String field : fields) if (node.hasNonNull(field)) return node.path(field).asInt();
        return null;
    }

    private Double doubleOrNull(JsonNode node, String... fields) {
        for (String field : fields) if (node.hasNonNull(field)) return node.path(field).asDouble();
        return null;
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) if (node.hasNonNull(field) && !node.path(field).asText().isBlank()) return node.path(field).asText();
        return null;
    }

    private String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.path(field).asText() : null;
    }

    private LocalDate localDateOrNull(JsonNode node, String field) {
        String value = textOrNull(node, field);
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
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
