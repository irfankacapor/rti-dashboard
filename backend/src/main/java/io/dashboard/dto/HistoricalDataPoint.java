package io.dashboard.dto;

public class HistoricalDataPoint {
    private String timestamp;
    private Double value;

    public HistoricalDataPoint() {}

    public HistoricalDataPoint(String timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
} 