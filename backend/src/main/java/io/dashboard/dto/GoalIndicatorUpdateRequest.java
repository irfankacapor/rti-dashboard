package io.dashboard.dto;

import io.dashboard.model.ImpactDirection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoalIndicatorUpdateRequest {
    
    @NotNull(message = "Aggregation weight is required")
    @DecimalMin(value = "0.0", message = "Aggregation weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Aggregation weight must be at most 1.0")
    private Double aggregationWeight;
    
    @NotNull(message = "Impact direction is required")
    private ImpactDirection impactDirection;
} 