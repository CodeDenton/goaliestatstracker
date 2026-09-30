package com.creasevision.api.ingestion;

import org.springframework.stereotype.Component;
import com.creasevision.api.model.Goalie;
import com.creasevision.api.repository.GoalieRepository;

@Component
public class JpaGoalieProfileStore implements GoalieProfileStore {
    private final GoalieRepository goalies;
    public JpaGoalieProfileStore(GoalieRepository goalies) { this.goalies = goalies; }
    public void saveProfile(Goalie imported) {
        Goalie stored = goalies.findById(imported.getId()).orElseGet(Goalie::new);
        stored.setId(imported.getId()); stored.setFirstName(imported.getFirstName()); stored.setLastName(imported.getLastName());
        stored.setHeadshot(imported.getHeadshot()); stored.setHeightCm(imported.getHeightCm()); stored.setWeightKg(imported.getWeightKg());
        stored.setCatches(imported.getCatches()); stored.setBirthDate(imported.getBirthDate()); stored.setBirthCity(imported.getBirthCity());
        stored.setBirthStateProvince(imported.getBirthStateProvince()); stored.setBirthCountry(imported.getBirthCountry());
        stored.setNationality(imported.getNationality()); stored.setPositionCode(imported.getPositionCode()); stored.setProfileUpdatedAt(imported.getProfileUpdatedAt());
        goalies.save(stored);
    }
}
