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
    
    @Query("SELECT DISTINCT i FROM Indicator i JOIN i.subareaIndicators si WHERE si.subarea.id = :subareaId")
    List<Indicator> findBySubareaId(Long subareaId);
} 