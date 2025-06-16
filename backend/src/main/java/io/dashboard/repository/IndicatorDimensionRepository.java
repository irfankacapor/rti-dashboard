package io.dashboard.repository;

import io.dashboard.model.DimensionType;
import io.dashboard.model.IndicatorDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicatorDimensionRepository extends JpaRepository<IndicatorDimension, Long> {
    
    List<IndicatorDimension> findByIndicatorId(Long indicatorId);
    
    List<IndicatorDimension> findByDimensionType(DimensionType dimensionType);
    
    List<IndicatorDimension> findByIndicatorIdAndDimensionType(Long indicatorId, DimensionType dimensionType);
    
    Optional<IndicatorDimension> findByIndicatorIdAndColumnHeader(Long indicatorId, String columnHeader);
    
    List<IndicatorDimension> findByIsPrimaryTrue();
    
    List<IndicatorDimension> findByIndicatorIdAndIsPrimaryTrue(Long indicatorId);
    
    @Query("SELECT id FROM IndicatorDimension id WHERE id.indicator.id = :indicatorId AND id.dimensionType = :dimensionType AND id.isPrimary = true")
    Optional<IndicatorDimension> findPrimaryByIndicatorAndType(@Param("indicatorId") Long indicatorId, @Param("dimensionType") DimensionType dimensionType);
    
    @Query("SELECT id FROM IndicatorDimension id WHERE id.columnHeader LIKE %:header%")
    List<IndicatorDimension> findByColumnHeaderContaining(@Param("header") String header);
} 