package com.creasevision.api.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.Team;
public interface TeamRepository extends JpaRepository<Team, Integer> { Optional<Team> findByAbbreviation(String abbreviation); }
