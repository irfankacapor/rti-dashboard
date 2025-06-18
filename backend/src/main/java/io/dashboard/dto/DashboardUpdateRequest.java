package io.dashboard.dto;

import io.dashboard.model.LayoutType;

public class DashboardUpdateRequest {
    private String name;
    private String description;
    private Long defaultLocationId;
    private Integer defaultYear;
    private LayoutType layoutType;

    public DashboardUpdateRequest() {}

    public DashboardUpdateRequest(String name, String description, Long defaultLocationId, Integer defaultYear, LayoutType layoutType) {
        this.name = name;
        this.description = description;
        this.defaultLocationId = defaultLocationId;
        this.defaultYear = defaultYear;
        this.layoutType = layoutType;
    }

    // Getters and setters
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
} 