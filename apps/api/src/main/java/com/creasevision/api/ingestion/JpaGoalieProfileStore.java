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
        stored.setId(imported.getId());
        if (imported.getFirstName()!=null) stored.setFirstName(imported.getFirstName()); if (imported.getLastName()!=null) stored.setLastName(imported.getLastName());
        if (imported.getHeadshot()!=null) stored.setHeadshot(imported.getHeadshot()); if (imported.getHeightCm()!=null) stored.setHeightCm(imported.getHeightCm()); if (imported.getWeightKg()!=null) stored.setWeightKg(imported.getWeightKg());
        if (imported.getCatches()!=null) stored.setCatches(imported.getCatches()); if (imported.getBirthDate()!=null) stored.setBirthDate(imported.getBirthDate()); if (imported.getBirthCity()!=null) stored.setBirthCity(imported.getBirthCity());
        if (imported.getBirthStateProvince()!=null) stored.setBirthStateProvince(imported.getBirthStateProvince()); if (imported.getBirthCountry()!=null) stored.setBirthCountry(imported.getBirthCountry());
        if (imported.getNationality()!=null) stored.setNationality(imported.getNationality()); if (imported.getPositionCode()!=null) stored.setPositionCode(imported.getPositionCode()); if (imported.getProfileUpdatedAt()!=null) stored.setProfileUpdatedAt(imported.getProfileUpdatedAt());
        goalies.save(stored);
    }
}
