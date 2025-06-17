package io.dashboard.repository;

import io.dashboard.model.FactIndicatorValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactIndicatorValueRepository extends JpaRepository<FactIndicatorValue, Long> {
    
    // Find by indicator
    List<FactIndicatorValue> findByIndicatorId(Long indicatorId);
    
    // Find by indicator and time range
    @Query("SELECT f FROM FactIndicatorValue f WHERE f.indicator.id = :indicatorId AND f.time.year BETWEEN :startYear AND :endYear")
    List<FactIndicatorValue> findByIndicatorIdAndTimeRange(@Param("indicatorId") Long indicatorId, 
                                                          @Param("startYear") Integer startYear, 
                                                          @Param("endYear") Integer endYear);
    
    // Find by indicator and location
    List<FactIndicatorValue> findByIndicatorIdAndLocationId(Long indicatorId, Long locationId);
    
    // Find by source row hash (for deduplication)
    Optional<FactIndicatorValue> findBySourceRowHash(String sourceRowHash);
    
    // Check if source row hash exists
    boolean existsBySourceRowHash(String sourceRowHash);
    
    // Find aggregated values
    List<FactIndicatorValue> findByIndicatorIdAndIsAggregatedTrue(Long indicatorId);
    
    // Find by value range
    @Query("SELECT f FROM FactIndicatorValue f WHERE f.indicator.id = :indicatorId AND f.value BETWEEN :minValue AND :maxValue")
    List<FactIndicatorValue> findByIndicatorIdAndValueRange(@Param("indicatorId") Long indicatorId,
                                                           @Param("minValue") BigDecimal minValue,
                                                           @Param("maxValue") BigDecimal maxValue);
    
    // Find latest values for an indicator
    @Query("SELECT f FROM FactIndicatorValue f WHERE f.indicator.id = :indicatorId ORDER BY f.time.year DESC, f.time.month DESC, f.time.day DESC")
    List<FactIndicatorValue> findLatestByIndicatorId(@Param("indicatorId") Long indicatorId);
    
    // Count by indicator
    long countByIndicatorId(Long indicatorId);
    
    // Find by confidence score threshold
    List<FactIndicatorValue> findByIndicatorIdAndConfidenceScoreGreaterThanEqual(Long indicatorId, Double confidenceScore);
    
    // Find by creation date range
    List<FactIndicatorValue> findByIndicatorIdAndCreatedAtBetween(Long indicatorId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Custom query for multi-dimensional filtering
    @Query("SELECT f FROM FactIndicatorValue f WHERE " +
           "(:indicatorId IS NULL OR f.indicator.id = :indicatorId) AND " +
           "(:timeId IS NULL OR f.time.id = :timeId) AND " +
           "(:locationId IS NULL OR f.location.id = :locationId) AND " +
           "(:genericId IS NULL OR f.generic.id = :genericId) AND " +
           "(:unitId IS NULL OR f.unit.id = :unitId)")
    List<FactIndicatorValue> findByDimensions(@Param("indicatorId") Long indicatorId,
                                             @Param("timeId") Long timeId,
                                             @Param("locationId") Long locationId,
                                             @Param("genericId") Long genericId,
                                             @Param("unitId") Long unitId);
    
    // Get distinct indicators
    @Query("SELECT DISTINCT f.indicator.id FROM FactIndicatorValue f")
    List<Long> findDistinctIndicatorIds();
    
    // Get value statistics for an indicator
    @Query("SELECT MIN(f.value), MAX(f.value), AVG(f.value), COUNT(f.value) FROM FactIndicatorValue f WHERE f.indicator.id = :indicatorId")
    Object[] getValueStatistics(@Param("indicatorId") Long indicatorId);
} 