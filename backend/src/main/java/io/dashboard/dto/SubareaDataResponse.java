package io.dashboard.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubareaDataResponse {
    
    // Subarea info (from /subareas/{subareaId})
    private SubareaResponse subarea;
    
    // Indicators list with latest values (from /subareas/{subareaId}/indicators)
    private List<IndicatorResponse> indicators;
    
    // Aggregated data by dimension (from /subareas/{subareaId}/aggregated-by-dimension)
    private Map<String, Map<String, Double>> aggregatedData;
    
    // Total aggregated value (from /subareas/{subareaId}/aggregated-value)
    private Double totalAggregatedValue;
    
    // Dimension metadata for all indicators (from /subareas/{subareaId}/indicators/{indicatorId}/dimension-values)
    private Map<String, IndicatorDimensionsResponse> dimensionMetadata;
    
    // Time series data for all indicators in the subarea
    // Structure: { year: string, indicators: { [indicatorName]: value } }
    private List<Map<String, Object>> timeSeriesData;
    
    // Individual indicator time series data
    // Structure: { [indicatorId]: { year: string, value: number }[] }
    private Map<String, List<Map<String, Object>>> indicatorTimeSeriesData;
    
    // Error tracking for partial failures
    private Map<String, String> errors;
} 