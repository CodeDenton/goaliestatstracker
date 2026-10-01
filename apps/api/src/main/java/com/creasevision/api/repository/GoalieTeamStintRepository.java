package com.creasevision.api.repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.GoalieTeamStint;
public interface GoalieTeamStintRepository extends JpaRepository<GoalieTeamStint, Long> { Optional<GoalieTeamStint> findByGoalieIdAndSeasonIdAndTeamIdAndStintNumber(Long goalieId, String seasonId, Integer teamId, short stintNumber); List<GoalieTeamStint> findBySeasonId(String seasonId); List<GoalieTeamStint> findByGoalieIdAndSeasonId(Long goalieId,String seasonId); List<GoalieTeamStint> findByGoalieIdOrderBySeasonIdDesc(Long goalieId); }
