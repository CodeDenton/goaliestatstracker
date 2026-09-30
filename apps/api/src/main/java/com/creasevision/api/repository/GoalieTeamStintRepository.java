package com.creasevision.api.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.GoalieTeamStint;
public interface GoalieTeamStintRepository extends JpaRepository<GoalieTeamStint, Long> { Optional<GoalieTeamStint> findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(Long goalieId, String seasonId, Integer teamId, short stintNumber); }
