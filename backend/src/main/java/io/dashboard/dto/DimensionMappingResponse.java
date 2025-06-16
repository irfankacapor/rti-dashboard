package io.dashboard.dto;

import io.dashboard.model.DimensionType;
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
public class DimensionMappingResponse {
    
    private Long mappingId;
    private Integer columnIndex;
    private String columnHeader;
    private DimensionType dimensionType;
    private Double confidenceScore;
    private Boolean isAutoDetected;
    private Map<String, Object> mappingRules;
    private List<String> suggestions;
    private String reason;
} 