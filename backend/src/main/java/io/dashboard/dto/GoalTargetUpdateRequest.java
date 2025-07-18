package io.dashboard.dto;

import io.dashboard.model.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalTargetUpdateRequest {
    
    @NotNull(message = "Target year is required")
    private Integer targetYear;
    
    @NotNull(message = "Target value is required")
    @Positive(message = "Target value must be positive")
    private BigDecimal targetValue;
    
    @NotNull(message = "Target type is required")
    private TargetType targetType;
    
} 