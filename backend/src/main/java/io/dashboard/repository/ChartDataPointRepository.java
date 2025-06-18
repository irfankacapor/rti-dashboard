package io.dashboard.repository;

import io.dashboard.entity.ChartDataPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ChartDataPointRepository extends JpaRepository<ChartDataPoint, Long> {
    List<ChartDataPoint> findByConfigIdOrderByTimestamp(Long configId);
    List<ChartDataPoint> findByConfigIdAndTimestampBetween(Long configId, LocalDateTime start, LocalDateTime end);
} 