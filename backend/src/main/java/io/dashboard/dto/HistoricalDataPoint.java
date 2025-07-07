package io.dashboard.dto;

import java.util.Map;

public class HistoricalDataPoint {
    private String timestamp;
    private Double value;
    private Map<String, String> dimensions;

    public HistoricalDataPoint() {}

    public HistoricalDataPoint(String timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public HistoricalDataPoint(String timestamp, Double value, Map<String, String> dimensions) {
        this.timestamp = timestamp;
        this.value = value;
        this.dimensions = dimensions;
    }

    // Getters and setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    
    public Map<String, String> getDimensions() { return dimensions; }
    public void setDimensions(Map<String, String> dimensions) { this.dimensions = dimensions; }
} 