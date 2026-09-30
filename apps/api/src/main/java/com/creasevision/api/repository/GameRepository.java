package com.creasevision.api.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.creasevision.api.model.Game;
public interface GameRepository extends JpaRepository<Game, Long> { }
