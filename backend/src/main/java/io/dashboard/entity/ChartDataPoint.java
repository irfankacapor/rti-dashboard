package io.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class ChartDataPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long configId;
    private String xValue;
    private Double yValue;
    private String label;
    @Lob
    private String metadata;
    private LocalDateTime timestamp;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public String getXValue() { return xValue; }
    public void setXValue(String xValue) { this.xValue = xValue; }
    public Double getYValue() { return yValue; }
    public void setYValue(Double yValue) { this.yValue = yValue; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChartDataPoint that = (ChartDataPoint) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(configId, that.configId) &&
                Objects.equals(xValue, that.xValue) &&
                Objects.equals(yValue, that.yValue) &&
                Objects.equals(label, that.label) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, configId, xValue, yValue, label, metadata, timestamp);
    }

    @Override
    public String toString() {
        return "ChartDataPoint{" +
                "id=" + id +
                ", configId=" + configId +
                ", xValue='" + xValue + '\'' +
                ", yValue=" + yValue +
                ", label='" + label + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 