package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiDimensionalAnalysis {
    
    private String orientation; // "ROWS" or "COLUMNS" for indicator placement
    private List<String> indicatorAxis;
    private List<String> timeAxis;
    private List<String> locationAxis;
    private List<String> additionalAxes;
    private Map<String, Object> valueCoordinates;
    private Integer totalDimensions;
    private Integer totalValues;
    private Boolean isComplete;
    private List<String> missingDimensions;
} 