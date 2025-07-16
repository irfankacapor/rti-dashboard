package io.dashboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class IndicatorValueCreate {
    @NotNull(message = "Dimensions are required")
    private Map<String, String> dimensions;
    
    @NotNull(message = "Value is required")
    private BigDecimal value;
} 