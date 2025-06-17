package io.dashboard.dto.goal;

import io.dashboard.entity.GoalIndicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalIndicatorResponse {
    
    private Long id;
    private Long indicatorId;
    private String indicatorName;
    private BigDecimal aggregationWeight;
    private GoalIndicator.ImpactDirection impactDirection;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 