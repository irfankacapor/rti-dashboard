package io.dashboard.repository;

import io.dashboard.model.PerformanceScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceScoreRepository extends JpaRepository<PerformanceScore, Long> {
    List<PerformanceScore> findBySubareaId(Long subareaId);
} 