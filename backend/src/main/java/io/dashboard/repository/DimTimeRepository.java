package io.dashboard.repository;

import io.dashboard.model.DimTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimTimeRepository extends JpaRepository<DimTime, Long> {
    
    Optional<DimTime> findByValue(String value);
    
    Optional<DimTime> findByYearAndMonthAndDay(Integer year, Integer month, Integer day);
    
    List<DimTime> findByYear(Integer year);
    
    List<DimTime> findByYearAndMonth(Integer year, Integer month);
    
    @Query("SELECT dt FROM DimTime dt WHERE dt.year = :year AND dt.quarter = :quarter")
    List<DimTime> findByYearAndQuarter(@Param("year") Integer year, @Param("quarter") Integer quarter);
    
    @Query("SELECT DISTINCT dt.year FROM DimTime dt ORDER BY dt.year")
    List<Integer> findAllYears();
    
    @Query("SELECT DISTINCT dt.month FROM DimTime dt WHERE dt.year = :year ORDER BY dt.month")
    List<Integer> findMonthsByYear(@Param("year") Integer year);
} 