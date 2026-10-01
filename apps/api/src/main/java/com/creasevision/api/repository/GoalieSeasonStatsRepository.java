package com.creasevision.api.repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.GoalieSeasonStats;
public interface GoalieSeasonStatsRepository extends JpaRepository<GoalieSeasonStats, Long> { Optional<GoalieSeasonStats> findByTeamStintId(Long teamStintId); List<GoalieSeasonStats> findByTeamStintIdIn(List<Long> teamStintIds); }
