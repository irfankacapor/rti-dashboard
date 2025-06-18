package io.dashboard.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class ChartDataRequest {
    @NotEmpty(message = "Indicator IDs cannot be empty")
    private List<Long> indicatorIds;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> locationIds;
    private String dimensionType;
    private String chartType;
    private String aggregationPeriod;

    // Getters and setters
    public List<Long> getIndicatorIds() { return indicatorIds; }
    public void setIndicatorIds(List<Long> indicatorIds) { this.indicatorIds = indicatorIds; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public List<Long> getLocationIds() { return locationIds; }
    public void setLocationIds(List<Long> locationIds) { this.locationIds = locationIds; }
    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    public String getChartType() { return chartType; }
    public void setChartType(String chartType) { this.chartType = chartType; }
    public String getAggregationPeriod() { return aggregationPeriod; }
    public void setAggregationPeriod(String aggregationPeriod) { this.aggregationPeriod = aggregationPeriod; }
} 