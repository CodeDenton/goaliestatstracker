package com.creasevision.api.repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.creasevision.api.model.GoalieGameStats;
public interface GoalieGameStatsRepository extends JpaRepository<GoalieGameStats, Long> {
    Optional<GoalieGameStats> findByGameIdAndGoalieId(Long gameId, Long goalieId);
    List<GoalieGameStats> findByGoalieId(Long goalieId);
    List<GoalieGameStats> findByGameId(Long gameId);
    @Query("select s from GoalieGameStats s join Game g on g.id = s.gameId where s.goalieId = :goalieId order by g.gameDate desc, s.gameId desc")
    List<GoalieGameStats> findByGoalieIdNewestFirst(Long goalieId);
}
