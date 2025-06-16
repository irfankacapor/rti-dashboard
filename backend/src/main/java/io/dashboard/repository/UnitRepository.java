package io.dashboard.repository;

import io.dashboard.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByCode(String code);
    boolean existsByCode(String code);
    
    @Query("SELECT COUNT(i) > 0 FROM Indicator i WHERE i.unit.id = :unitId")
    boolean hasIndicators(@Param("unitId") Long unitId);
} 