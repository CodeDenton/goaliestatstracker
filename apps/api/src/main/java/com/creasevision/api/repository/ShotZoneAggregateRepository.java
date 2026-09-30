package com.creasevision.api.repository;
import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import com.creasevision.api.model.ShotZoneAggregate;
public interface ShotZoneAggregateRepository extends JpaRepository<ShotZoneAggregate,Long> { Optional<ShotZoneAggregate> findByGoalieIdAndSeasonIdAndGameIdAndTeamIdAndScopeAndZoneCode(Long goalieId,String seasonId,Long gameId,Integer teamId,String scope,String zoneCode); }
