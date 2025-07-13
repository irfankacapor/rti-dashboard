package io.dashboard.repository;

import io.dashboard.model.Indicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface IndicatorRepository extends JpaRepository<Indicator, Long> {
    Optional<Indicator> findByCode(String code);
    Optional<Indicator> findByName(String name);
    boolean existsByCode(String code);
    
    @Query("SELECT i FROM Indicator i LEFT JOIN FETCH i.goalIndicators gi LEFT JOIN FETCH gi.goal WHERE i.id = :indicatorId")
    Indicator findByIdWithGoals(Long indicatorId);

    @Query("SELECT DISTINCT i FROM Indicator i JOIN FactIndicatorValue f ON f.indicator.id = i.id WHERE f.subarea.id = :subareaId")
    List<Indicator> findByFactSubareaId(Long subareaId);
} 