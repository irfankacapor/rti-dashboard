package io.dashboard.dto;

import java.util.Map;

public class DataAggregationResponse {
    private Long indicatorId;
    private String aggregationType;
    private Map<String, Object> data;

    public DataAggregationResponse() {}

    public DataAggregationResponse(Long indicatorId, String aggregationType, Map<String, Object> data) {
        this.indicatorId = indicatorId;
        this.aggregationType = aggregationType;
        this.data = data;
    }

    // Getters and setters
    public Long getIndicatorId() { return indicatorId; }
    public void setIndicatorId(Long indicatorId) { this.indicatorId = indicatorId; }
    
    public String getAggregationType() { return aggregationType; }
    public void setAggregationType(String aggregationType) { this.aggregationType = aggregationType; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
} 