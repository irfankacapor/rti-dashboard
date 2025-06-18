package io.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PerformanceMetricsResponse {
    private Long areaId;
    private List<PerformanceMetric> metrics;
    private Double averageScore;

    public PerformanceMetricsResponse() {}

    public PerformanceMetricsResponse(Long areaId, List<PerformanceMetric> metrics, Double averageScore) {
        this.areaId = areaId;
        this.metrics = metrics;
        this.averageScore = averageScore;
    }

    // Getters and setters
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    
    public List<PerformanceMetric> getMetrics() { return metrics; }
    public void setMetrics(List<PerformanceMetric> metrics) { this.metrics = metrics; }
    
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
} 