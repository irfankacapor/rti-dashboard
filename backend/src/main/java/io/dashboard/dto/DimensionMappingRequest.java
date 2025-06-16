package io.dashboard.dto;

import io.dashboard.model.DimensionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionMappingRequest {
    
    private Integer columnIndex;
    private DimensionType dimensionType;
    private String columnHeader;
    private Map<String, Object> mappingRules;
    private Boolean isPrimary;
} 