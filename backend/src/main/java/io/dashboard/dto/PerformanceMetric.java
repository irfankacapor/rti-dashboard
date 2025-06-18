package io.dashboard.dto;

import java.time.LocalDateTime;

public class PerformanceMetric {
    private Long subareaId;
    private String subareaName;
    private Double currentScore;
    private String colorCode;
    private String trend;
    private LocalDateTime lastUpdated;

    public PerformanceMetric() {}

    public PerformanceMetric(Long subareaId, String subareaName, Double currentScore, String colorCode, String trend, LocalDateTime lastUpdated) {
        this.subareaId = subareaId;
        this.subareaName = subareaName;
        this.currentScore = currentScore;
        this.colorCode = colorCode;
        this.trend = trend;
        this.lastUpdated = lastUpdated;
    }

    // Getters and setters
    public Long getSubareaId() { return subareaId; }
    public void setSubareaId(Long subareaId) { this.subareaId = subareaId; }
    
    public String getSubareaName() { return subareaName; }
    public void setSubareaName(String subareaName) { this.subareaName = subareaName; }
    
    public Double getCurrentScore() { return currentScore; }
    public void setCurrentScore(Double currentScore) { this.currentScore = currentScore; }
    
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
} 