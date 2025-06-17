package io.dashboard.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgressResponse {
    
    private Long goalId;
    private String goalName;
    private BigDecimal overallProgress;
    private List<IndicatorProgressInfo> indicatorProgress;
    private Integer totalIndicators;
    private Integer indicatorsWithTargets;
} 