package com.creasevision.api.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

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
}
