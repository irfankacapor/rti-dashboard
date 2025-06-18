package io.dashboard.repository;

import io.dashboard.entity.VisualizationConfig;
import io.dashboard.entity.VisualizationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisualizationConfigRepository extends JpaRepository<VisualizationConfig, Long> {
    List<VisualizationConfig> findByIndicatorId(Long indicatorId);
    List<VisualizationConfig> findByIndicatorIdAndIsDefault(Long indicatorId, Boolean isDefault);
    List<VisualizationConfig> findByVisualizationType(VisualizationType type);
} 