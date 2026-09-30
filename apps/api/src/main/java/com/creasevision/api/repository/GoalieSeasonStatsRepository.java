package com.creasevision.api.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.GoalieSeasonStats;
public interface GoalieSeasonStatsRepository extends JpaRepository<GoalieSeasonStats, Long> { Optional<GoalieSeasonStats> findByTeamStintId(Long teamStintId); }
