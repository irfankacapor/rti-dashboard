package io.dashboard.repository;

import io.dashboard.model.CsvAnalysis;
import io.dashboard.model.CsvColumnMapping;
import io.dashboard.model.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CsvColumnMappingRepository extends JpaRepository<CsvColumnMapping, Long> {
    
    List<CsvColumnMapping> findByAnalysisId(Long analysisId);
    
    List<CsvColumnMapping> findByAnalysisIdAndDimensionType(Long analysisId, DimensionType dimensionType);
    
    Optional<CsvColumnMapping> findByAnalysisIdAndColumnIndex(Long analysisId, Integer columnIndex);
    
    List<CsvColumnMapping> findByAnalysisIdAndIsAutoDetectedTrue(Long analysisId);
    
    List<CsvColumnMapping> findByAnalysisIdAndIsAutoDetectedFalse(Long analysisId);
    
    @Query("SELECT ccm FROM CsvColumnMapping ccm WHERE ccm.analysis.id = :analysisId AND ccm.confidenceScore >= :minConfidence")
    List<CsvColumnMapping> findByAnalysisIdAndMinConfidence(@Param("analysisId") Long analysisId, @Param("minConfidence") Double minConfidence);
    
    @Query("SELECT ccm FROM CsvColumnMapping ccm WHERE ccm.analysis.id = :analysisId AND ccm.columnHeader LIKE %:header%")
    List<CsvColumnMapping> findByAnalysisIdAndColumnHeaderContaining(@Param("analysisId") Long analysisId, @Param("header") String header);
    
    @Query("SELECT ccm FROM CsvColumnMapping ccm WHERE ccm.analysis.id = :analysisId ORDER BY ccm.columnIndex")
    List<CsvColumnMapping> findByAnalysisIdOrderByColumnIndex(@Param("analysisId") Long analysisId);
    
    @Query("SELECT COUNT(ccm) FROM CsvColumnMapping ccm WHERE ccm.analysis.id = :analysisId AND ccm.dimensionType = :dimensionType")
    long countByAnalysisIdAndDimensionType(@Param("analysisId") Long analysisId, @Param("dimensionType") DimensionType dimensionType);
} 