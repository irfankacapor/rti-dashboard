package io.dashboard.dto.goal;

import io.dashboard.enums.ImpactDirection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalIndicatorLinkRequest {
    
    @NotNull(message = "Indicator ID is required")
    private Long indicatorId;
    
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Weight must be at most 1.0")
    private BigDecimal weight;
    
    @NotNull(message = "Impact direction is required")
    private ImpactDirection impactDirection;
} 