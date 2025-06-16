package io.dashboard.repository;

import io.dashboard.model.SubareaIndicator;
import io.dashboard.model.SubareaIndicator.SubareaIndicatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubareaIndicatorRepository extends JpaRepository<SubareaIndicator, SubareaIndicatorId> {
    List<SubareaIndicator> findBySubareaId(Long subareaId);
    List<SubareaIndicator> findByIndicatorId(Long indicatorId);
    Optional<SubareaIndicator> findBySubareaIdAndIndicatorId(Long subareaId, Long indicatorId);
    boolean existsBySubareaIdAndIndicatorId(Long subareaId, Long indicatorId);
} 