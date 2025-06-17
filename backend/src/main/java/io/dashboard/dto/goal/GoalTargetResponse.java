package io.dashboard.dto.goal;

import io.dashboard.model.GoalTarget;
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
public class GoalTargetResponse {
    
    private Long id;
    private Long indicatorId;
    private String indicatorName;
    private Integer targetYear;
    private BigDecimal targetValue;
    private GoalTarget.TargetType targetType;
    private BigDecimal targetPer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 