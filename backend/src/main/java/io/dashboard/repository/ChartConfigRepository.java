package io.dashboard.repository;

import io.dashboard.entity.ChartConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChartConfigRepository extends JpaRepository<ChartConfig, Long> {
} 