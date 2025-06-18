package io.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RealTimeUpdateResponse {
    private Long dashboardId;
    private List<WidgetUpdate> updates;
    private LocalDateTime timestamp;

    public RealTimeUpdateResponse() {}

    public RealTimeUpdateResponse(Long dashboardId, List<WidgetUpdate> updates, LocalDateTime timestamp) {
        this.dashboardId = dashboardId;
        this.updates = updates;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getDashboardId() { return dashboardId; }
    public void setDashboardId(Long dashboardId) { this.dashboardId = dashboardId; }
    
    public List<WidgetUpdate> getUpdates() { return updates; }
    public void setUpdates(List<WidgetUpdate> updates) { this.updates = updates; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 