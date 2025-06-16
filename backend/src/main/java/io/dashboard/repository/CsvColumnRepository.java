package io.dashboard.repository;

import io.dashboard.model.CsvColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CsvColumnRepository extends JpaRepository<CsvColumn, Long> {
    List<CsvColumn> findByCsvAnalysisId(Long analysisId);
} 