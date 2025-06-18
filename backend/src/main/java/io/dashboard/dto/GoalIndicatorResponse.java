package io.dashboard.dto;

import io.dashboard.model.ImpactDirection;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoalIndicatorResponse {
    
    private Long goalId;
    private String goalName;
    private Long indicatorId;
    private String indicatorName;
    private String indicatorCode;
    private Double aggregationWeight;
    private ImpactDirection impactDirection;
    private LocalDateTime createdAt;
} 