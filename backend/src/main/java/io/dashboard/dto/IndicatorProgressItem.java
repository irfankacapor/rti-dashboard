package io.dashboard.dto;

import io.dashboard.model.ImpactDirection;
import lombok.Data;

@Data
public class IndicatorProgressItem {
    
    private Long indicatorId;
    private String indicatorName;
    private String indicatorCode;
    private Double currentValue;
    private Double targetValue;
    private Double progress;
    private Double weight;
    private ImpactDirection direction;
    private String unit;
} 