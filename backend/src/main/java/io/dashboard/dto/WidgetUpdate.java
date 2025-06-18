package io.dashboard.dto;

import java.time.LocalDateTime;

public class WidgetUpdate {
    private Long widgetId;
    private LocalDateTime lastUpdated;
    private Boolean hasChanges;

    public WidgetUpdate() {}

    public WidgetUpdate(Long widgetId, LocalDateTime lastUpdated, Boolean hasChanges) {
        this.widgetId = widgetId;
        this.lastUpdated = lastUpdated;
        this.hasChanges = hasChanges;
    }

    // Getters and setters
    public Long getWidgetId() { return widgetId; }
    public void setWidgetId(Long widgetId) { this.widgetId = widgetId; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public Boolean getHasChanges() { return hasChanges; }
    public void setHasChanges(Boolean hasChanges) { this.hasChanges = hasChanges; }
} 