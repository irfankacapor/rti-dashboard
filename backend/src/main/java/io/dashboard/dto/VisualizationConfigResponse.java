package io.dashboard.dto;

import io.dashboard.entity.VisualizationType;
import java.time.LocalDateTime;

public class VisualizationConfigResponse {
    private Long id;
    private Long indicatorId;
    private VisualizationType visualizationType;
    private String title;
    private String config;
    private boolean isDefault;
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 