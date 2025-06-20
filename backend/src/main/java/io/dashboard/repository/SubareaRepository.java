package io.dashboard.repository;

import io.dashboard.model.Subarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface SubareaRepository extends JpaRepository<Subarea, Long> {
    Optional<Subarea> findByCode(String code);
    boolean existsByCode(String code);
    List<Subarea> findByAreaId(Long areaId);
    
    @Query("SELECT s FROM Subarea s LEFT JOIN FETCH s.area LEFT JOIN FETCH s.subareaIndicators")
    List<Subarea> findAllWithAreaAndIndicators();
} 