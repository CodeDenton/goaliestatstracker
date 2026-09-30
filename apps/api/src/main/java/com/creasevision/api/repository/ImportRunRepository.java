package com.creasevision.api.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.creasevision.api.model.ImportRun;

public interface ImportRunRepository extends JpaRepository<ImportRun, Long> {

    Optional<ImportRun> findTopByImportTypeOrderByStartedAtDesc(String importType);
}
