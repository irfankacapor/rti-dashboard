package io.dashboard.dto;

import java.time.LocalDateTime;

public class IndicatorSummaryResponse {
    private Long id;
    private String name;
    private Double latestValue;
    private String trend;
    private String unit;
    private LocalDateTime lastUpdated;

    public IndicatorSummaryResponse() {}

    public IndicatorSummaryResponse(Long id, String name, Double latestValue, String trend, String unit, LocalDateTime lastUpdated) {
        this.id = id;
        this.name = name;
        this.latestValue = latestValue;
        this.trend = trend;
        this.unit = unit;
        this.lastUpdated = lastUpdated;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatestValue() { return latestValue; }
    public void setLatestValue(Double latestValue) { this.latestValue = latestValue; }
    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
} 