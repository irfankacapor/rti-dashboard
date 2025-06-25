package io.dashboard.repository;

import io.dashboard.model.SubareaIndicator;
import io.dashboard.model.SubareaIndicator.SubareaIndicatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SubareaIndicatorRepository extends JpaRepository<SubareaIndicator, SubareaIndicatorId> {
    List<SubareaIndicator> findBySubareaId(Long subareaId);
    List<SubareaIndicator> findByIndicatorId(Long indicatorId);
    Optional<SubareaIndicator> findBySubareaIdAndIndicatorId(Long subareaId, Long indicatorId);
    boolean existsBySubareaIdAndIndicatorId(Long subareaId, Long indicatorId);
    @Query("SELECT si FROM SubareaIndicator si JOIN FETCH si.subarea WHERE si.indicator.id = :indicatorId")
    List<SubareaIndicator> findByIndicatorIdWithSubarea(@Param("indicatorId") Long indicatorId);
} 