package com.creasevision.api.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.GoalieGameStats;
public interface GoalieGameStatsRepository extends JpaRepository<GoalieGameStats, Long> { Optional<GoalieGameStats> findByGameIdAndGoalieId(Long gameId, Long goalieId); }
