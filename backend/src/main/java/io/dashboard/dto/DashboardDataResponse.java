package io.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDataResponse {
    private List<AreaPerformanceResponse> areas;
    private PerformanceOverviewResponse performanceOverview;
    private LocalDateTime lastUpdated;

    public DashboardDataResponse() {}

    public DashboardDataResponse(List<AreaPerformanceResponse> areas, PerformanceOverviewResponse performanceOverview, LocalDateTime lastUpdated) {
        this.areas = areas;
        this.performanceOverview = performanceOverview;
        this.lastUpdated = lastUpdated;
    }

    // Getters and setters
    public List<AreaPerformanceResponse> getAreas() { return areas; }
    public void setAreas(List<AreaPerformanceResponse> areas) { this.areas = areas; }
    public PerformanceOverviewResponse getPerformanceOverview() { return performanceOverview; }
    public void setPerformanceOverview(PerformanceOverviewResponse performanceOverview) { this.performanceOverview = performanceOverview; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
} 