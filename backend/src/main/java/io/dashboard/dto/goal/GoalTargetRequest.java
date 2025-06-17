package io.dashboard.dto.goal;

import io.dashboard.model.GoalTarget;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class GoalTargetRequest {
    
    @NotNull(message = "Indicator ID is required")
    private Long indicatorId;
    
    @NotNull(message = "Target year is required")
    @Positive(message = "Target year must be positive")
    private Integer targetYear;
    
    @NotNull(message = "Target value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Target value must be positive")
    private BigDecimal targetValue;
    
    @NotNull(message = "Target type is required")
    private GoalTarget.TargetType targetType;
    
    @DecimalMin(value = "0.0", message = "Target percentage must be non-negative")
    private BigDecimal targetPer;
} 