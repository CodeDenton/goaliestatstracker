package com.creasevision.api.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.Season;
public interface SeasonRepository extends JpaRepository<Season, String> { }
