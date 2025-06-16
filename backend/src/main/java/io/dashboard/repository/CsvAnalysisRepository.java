package io.dashboard.repository;

import io.dashboard.model.CsvAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CsvAnalysisRepository extends JpaRepository<CsvAnalysis, Long> {
    List<CsvAnalysis> findByJobId(Long jobId);
    Optional<CsvAnalysis> findByJobIdAndFilename(Long jobId, String filename);
} 