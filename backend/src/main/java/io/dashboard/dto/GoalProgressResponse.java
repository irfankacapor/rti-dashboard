package io.dashboard.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoalProgressResponse {
    
    private Long goalId;
    private String goalName;
    private Double overallProgress;
    private List<IndicatorProgressItem> indicatorProgress;
    private Double totalWeight;
    private String progressStatus; // "ON_TRACK", "AT_RISK", "OFF_TRACK"
} 