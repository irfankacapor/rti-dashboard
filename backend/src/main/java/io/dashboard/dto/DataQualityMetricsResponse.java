package io.dashboard.dto;

import java.time.LocalDateTime;

public class DataQualityMetricsResponse {
    private Long dashboardId;
    private Double completeness;
    private Double accuracy;
    private Double timeliness;
    private Double overallScore;
    private LocalDateTime lastCalculated;

    public DataQualityMetricsResponse() {}

    public DataQualityMetricsResponse(Long dashboardId, Double completeness, Double accuracy, Double timeliness, Double overallScore, LocalDateTime lastCalculated) {
        this.dashboardId = dashboardId;
        this.completeness = completeness;
        this.accuracy = accuracy;
        this.timeliness = timeliness;
        this.overallScore = overallScore;
        this.lastCalculated = lastCalculated;
    }

    // Getters and setters
    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long dashboardId) { this.dashboardId = dashboardId; }
    
    public Double getCompleteness() { return completeness; }
    public void setCompleteness(Double completeness) { this.completeness = completeness; }
    
    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }
    
    public Double getTimeliness() { return timeliness; }
    public void setTimeliness(Double timeliness) { this.timeliness = timeliness; }
    
    public Double getOverallScore() { return overallScore; }
    public void setOverallScore(Double overallScore) { this.overallScore = overallScore; }
    
    public LocalDateTime getLastCalculated() { return lastCalculated; }
    public void setLastCalculated(LocalDateTime lastCalculated) { this.lastCalculated = lastCalculated; }
} 