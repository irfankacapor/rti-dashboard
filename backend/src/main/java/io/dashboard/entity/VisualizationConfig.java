package io.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class VisualizationConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long indicatorId;

    @Enumerated(EnumType.STRING)
    private VisualizationType visualizationType;

    @Lob
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
    public String getConfig() { return config; }
    public void setConfig(String config) { this.config = config; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisualizationConfig that = (VisualizationConfig) o;
        return isDefault == that.isDefault &&
                Objects.equals(id, that.id) &&
                Objects.equals(indicatorId, that.indicatorId) &&
                visualizationType == that.visualizationType &&
                Objects.equals(config, that.config) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, indicatorId, visualizationType, config, isDefault, createdAt);
    }

    @Override
    public String toString() {
        return "VisualizationConfig{" +
                "id=" + id +
                ", indicatorId=" + indicatorId +
                ", visualizationType=" + visualizationType +
                ", isDefault=" + isDefault +
                ", createdAt=" + createdAt +
                '}';
    }
} 