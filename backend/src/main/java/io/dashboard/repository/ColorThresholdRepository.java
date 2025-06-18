package io.dashboard.repository;

import io.dashboard.model.ColorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColorThresholdRepository extends JpaRepository<ColorThreshold, Long> {
} 