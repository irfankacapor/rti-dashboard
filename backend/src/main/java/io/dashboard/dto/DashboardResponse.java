package io.dashboard.dto;

import io.dashboard.model.LayoutType;
import java.time.LocalDateTime;

public class DashboardResponse {
    private Long id;
    private String name;
    private String description;
    private Long defaultLocationId;
    private Integer defaultYear;
    private LayoutType layoutType;
    private LocalDateTime createdAt;
    private Integer widgetCount;

    public DashboardResponse() {}

    public DashboardResponse(Long id, String name, String description, Long defaultLocationId, Integer defaultYear, LayoutType layoutType, LocalDateTime createdAt, Integer widgetCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultLocationId = defaultLocationId;
        this.defaultYear = defaultYear;
        this.layoutType = layoutType;
        this.createdAt = createdAt;
        this.widgetCount = widgetCount;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDefaultLocationId() { return defaultLocationId; }
    public void setDefaultLocationId(Long defaultLocationId) { this.defaultLocationId = defaultLocationId; }
    public Integer getDefaultYear() { return defaultYear; }
    public void setDefaultYear(Integer defaultYear) { this.defaultYear = defaultYear; }
    public LayoutType getLayoutType() { return layoutType; }
    public void setLayoutType(LayoutType layoutType) { this.layoutType = layoutType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getWidgetCount() { return widgetCount; }
    public void setWidgetCount(Integer widgetCount) { this.widgetCount = widgetCount; }
} 