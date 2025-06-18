package io.dashboard.dto;

import io.dashboard.entity.VisualizationType;

public class VisualizationConfigRequest {
    private Long indicatorId;
    private VisualizationType visualizationType;
    private String title;
    private String config;
    private boolean isDefault;

    // Getters and setters
    public Long getIndicatorId() { return indicatorId; }
    public void setIndicatorId(Long indicatorId) { this.indicatorId = indicatorId; }
    public VisualizationType getVisualizationType() { return visualizationType; }
    public void setVisualizationType(VisualizationType visualizationType) { this.visualizationType = visualizationType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
} 