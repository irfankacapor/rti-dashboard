package io.dashboard.dto;

import java.time.LocalDateTime;
import java.util.List;

public class HistoricalDataResponse {
    private Long indicatorId;
    private List<HistoricalDataPoint> dataPoints;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<String> dimensions;

    public HistoricalDataResponse() {}

    public HistoricalDataResponse(Long indicatorId, List<HistoricalDataPoint> dataPoints, LocalDateTime startDate, LocalDateTime endDate) {
        this.indicatorId = indicatorId;
        this.dataPoints = dataPoints;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public HistoricalDataResponse(Long indicatorId, List<HistoricalDataPoint> dataPoints, LocalDateTime startDate, LocalDateTime endDate, List<String> dimensions) {
        this.indicatorId = indicatorId;
        this.dataPoints = dataPoints;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dimensions = dimensions;
    }

    // Getters and setters
    public Long getIndicatorId() { return indicatorId; }
    public void setIndicatorId(Long indicatorId) { this.indicatorId = indicatorId; }
    
    public List<HistoricalDataPoint> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<HistoricalDataPoint> dataPoints) { this.dataPoints = dataPoints; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public List<String> getDimensions() { return dimensions; }
    public void setDimensions(List<String> dimensions) { this.dimensions = dimensions; }
} 