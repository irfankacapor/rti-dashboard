package io.dashboard.dto;

import java.time.LocalDateTime;

public class DataRefreshStatusResponse {
    private Long dashboardId;
    private LocalDateTime lastRefresh;
    private LocalDateTime nextRefresh;
    private String refreshInterval;
    private Boolean isAutoRefresh;
    private String status;

    public DataRefreshStatusResponse() {}

    public DataRefreshStatusResponse(Long dashboardId, LocalDateTime lastRefresh, LocalDateTime nextRefresh, String refreshInterval, Boolean isAutoRefresh, String status) {
        this.dashboardId = dashboardId;
        this.lastRefresh = lastRefresh;
        this.nextRefresh = nextRefresh;
        this.refreshInterval = refreshInterval;
        this.isAutoRefresh = isAutoRefresh;
        this.status = status;
    }

    // Getters and setters
    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long dashboardId) { this.dashboardId = dashboardId; }
    
    public LocalDateTime getLastRefresh() { return lastRefresh; }
    public void setLastRefresh(LocalDateTime lastRefresh) { this.lastRefresh = lastRefresh; }
    
    public LocalDateTime getNextRefresh() { return nextRefresh; }
    public void setNextRefresh(LocalDateTime nextRefresh) { this.nextRefresh = nextRefresh; }
    
    public String getRefreshInterval() { return refreshInterval; }
    public void setRefreshInterval(String refreshInterval) { this.refreshInterval = refreshInterval; }
    
    public Boolean getIsAutoRefresh() { return isAutoRefresh; }
    public void setIsAutoRefresh(Boolean isAutoRefresh) { this.isAutoRefresh = isAutoRefresh; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 