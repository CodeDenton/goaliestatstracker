package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.creasevision.api.model.Goalie;
import com.fasterxml.jackson.databind.ObjectMapper;

class NhlHttpGoalieClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NhlHttpGoalieClient client = new NhlHttpGoalieClient(null, objectMapper);

    @Test
    void mapsStableBiographyFromTheNhlRoster() throws Exception {
        Goalie goalie = client.toGoalie(
                objectMapper.readTree("""
                        { "player": {
                          "id": 8480280,
                          "firstName": { "default": "Jeremy" },
                          "lastName": { "default": "Swayman" },
                          "team": { "abbrev": "BOS", "commonName": { "default": "Bruins" }, "teamLogo": {} }
                        }}
                        """),
                objectMapper.readTree("""
                        {
                          "heightInCentimeters": 191,
                          "weightInKilograms": 89,
                          "shootsCatches": "L",
                          "birthDate": "1998-11-24",
                          "birthCity": { "default": "Anchorage" },
                          "birthStateProvince": { "default": "AK" },
                          "birthCountry": "USA",
                          "positionCode": "G"
                        }
                        """));

        assertThat(goalie.getHeightCm()).isEqualTo(191);
        assertThat(goalie.getWeightKg()).isEqualTo(89);
        assertThat(goalie.getCatches()).isEqualTo("L");
        assertThat(goalie.getBirthDate()).isEqualTo(LocalDate.of(1998, 11, 24));
        assertThat(goalie.getBirthCity()).isEqualTo("Anchorage");
        assertThat(goalie.getBirthStateProvince()).isEqualTo("AK");
        assertThat(goalie.getBirthCountry()).isEqualTo("USA");
        assertThat(goalie.getPositionCode()).isEqualTo("G");
        assertThat(goalie.getProfileUpdatedAt()).isNotNull();
    }

    @Test
    void mapsRegularSeasonTeamSplitRowsAndExcludesPlayoffAndAggregateRows() throws Exception {
        List<NhlGoalieSeasonRow> rows = client.toRegularSeasonTeamSplits(objectMapper.readTree("""
                { "data": [
                  { "gameTypeId": 2, "playerId": 8479973, "playerName": "Stuart Skinner", "teamAbbrevs": "EDM",
                    "gamesPlayed": 42, "wins": 22, "losses": 16, "otLosses": 3, "saves": 1057,
                    "shotsAgainst": 1149, "goalsAgainstAverage": 2.76, "savePct": 0.920 },
                  { "gameTypeId": 2, "playerId": 8479973, "playerName": "Stuart Skinner", "teamAbbrevs": "PIT",
                    "gamesPlayed": 18, "wins": 8, "losses": 8, "otLosses": 1, "saves": 441,
                    "shotsAgainst": 493, "goalsAgainstAverage": 2.91, "savePct": 0.894 },
                  { "gameTypeId": 3, "playerId": 8479973, "playerName": "Stuart Skinner", "teamAbbrevs": "PIT" },
                  { "gameTypeId": 2, "playerId": 8479973, "playerName": "Stuart Skinner", "teamAbbrevs": "EDM, PIT" }
                ] }
                """));

        assertThat(rows).extracting(NhlGoalieSeasonRow::teamAbbreviation).containsExactly("EDM", "PIT");
        assertThat(rows.getFirst()).extracting(NhlGoalieSeasonRow::gamesPlayed, NhlGoalieSeasonRow::saves,
                NhlGoalieSeasonRow::shotsAgainst, NhlGoalieSeasonRow::goalsAgainstAvg, NhlGoalieSeasonRow::savePctg)
                .containsExactly(42, 1057, 1149, 2.76, .920);
    }

    @Test
    void mapsGoaliesFromOneCompletedGameBoxscore() throws Exception {
        List<NhlGoalieGameRow> rows = client.toGoalieGameRows(objectMapper.readTree("""
                { "id": 2025020001, "season": "20252026", "gameDate": "2025-10-07", "gameType": 2, "gameState": "OFF",
                  "homeTeam": { "abbrev": "PIT", "score": 3 }, "awayTeam": { "abbrev": "EDM", "score": 2 },
                  "playerByGameStats": {
                    "homeTeam": { "goalies": [{ "playerId": 8477465, "starter": true, "decision": "W", "toi": "60:00", "saveShotsAgainst": "25/27", "goalsAgainst": 2 }] },
                    "awayTeam": { "goalies": [{ "playerId": 8479973, "starter": true, "decision": "L", "toi": "58:44", "saveShotsAgainst": "28/31", "goalsAgainst": 3 }] }
                  } }
                """));
        assertThat(rows).hasSize(2);
        assertThat(rows.getFirst()).extracting(NhlGoalieGameRow::gameId, NhlGoalieGameRow::team, NhlGoalieGameRow::saves, NhlGoalieGameRow::shotsAgainst, NhlGoalieGameRow::timeOnIceSeconds)
                .containsExactly(2025020001L, "PIT", 25, 27, 3600);
    }

    @Test
    void aggregatesPlayByPlayShotsIntoGoalieZones() throws Exception {
        List<NhlShotZoneRow> rows=client.toShotZoneRows(objectMapper.readTree("""
                {"id":2025020001,"season":"20252026","homeTeam":{"id":5,"abbrev":"PIT"},"awayTeam":{"abbrev":"EDM"},"plays":[
                  {"typeDescKey":"shot-on-goal","details":{"goalieInNetId":8477465,"eventOwnerTeamId":22,"xCoord":80,"yCoord":4}},
                  {"typeDescKey":"goal","details":{"goalieInNetId":8477465,"eventOwnerTeamId":22,"xCoord":76,"yCoord":8}},
                  {"typeDescKey":"shot-on-goal","details":{"goalieInNetId":8477465,"eventOwnerTeamId":22,"xCoord":30,"yCoord":30}}
                ]}"""));
        assertThat(rows).extracting(NhlShotZoneRow::zoneCode,NhlShotZoneRow::shots,NhlShotZoneRow::saves,NhlShotZoneRow::goals)
                .containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("INNER_SLOT",2,1,1),org.assertj.core.groups.Tuple.tuple("PERIMETER",1,1,0));
    }
}
