package io.dashboard.dto;

import io.dashboard.model.TargetType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalTargetResponse {
    
    private Long id;
    private GoalResponse goal;
    private IndicatorResponse indicator;
    private Integer targetYear;
    private BigDecimal targetValue;
    private TargetType targetType;
    private BigDecimal targetPercentage;
    private LocalDateTime createdAt;
} 