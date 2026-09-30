package com.creasevision.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.creasevision.api.model.Goalie;
import com.creasevision.api.model.ShotLocationDetail;
import com.creasevision.api.model.ShotLocationSummary;
import com.creasevision.api.repository.GoalieRepository;

@SpringBootTest
class GoalieServiceIntegrationTest {
    @Autowired GoalieService service;
    @Autowired GoalieRepository goalies;

    @AfterEach void cleanUp() { goalies.deleteAll(); }

    @Test
    void mapsLazyShotCollectionsWhileOpenInViewIsDisabled() {
        Goalie goalie = new Goalie(); goalie.setId(9001L); goalie.setFirstName("Test"); goalie.setLastName("Goalie");
        ShotLocationSummary summary = new ShotLocationSummary(); summary.setGoalie(goalie); summary.setLocationCode("ALL"); summary.setSaves(20); summary.setGoalsAgainst(2);
        ShotLocationDetail detail = new ShotLocationDetail(); detail.setGoalie(goalie); detail.setArea("SLOT"); detail.setSaves(5);
        goalie.setShotLocationSummary(List.of(summary)); goalie.setShotLocationDetails(List.of(detail));
        goalies.saveAndFlush(goalie);

        assertThat(service.getAllGoalies()).singleElement().satisfies(dto -> {
            assertThat(dto.getShotLocationSummary()).hasSize(1);
            assertThat(dto.getShotLocationDetails()).hasSize(1);
        });
    }
}
