package io.dashboard.repository;

import io.dashboard.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    Optional<Area> findByCode(String code);
    boolean existsByCode(String code);
    
    @Query("SELECT a FROM Area a LEFT JOIN FETCH a.subareas")
    List<Area> findAllWithSubareas();
    
    @Query("SELECT a FROM Area a LEFT JOIN FETCH a.subareas WHERE a.id = :id")
    Optional<Area> findByIdWithSubareas(Long id);
} 