package io.dashboard.dto;

import java.util.List;
import io.dashboard.dto.IndicatorProgressItem;

public class GoalProgressResponse {
    // Old fields for compatibility
    private Long goalId;
    private String goalName;
    private Double totalWeight;
    private String progressStatus;
    private List<IndicatorProgressItem> indicatorProgress;
    // New fields for dashboard
    private Long id;
    private String name;
    private Double overallProgress;
    private List<IndicatorSummaryResponse> linkedIndicators;
    private String status;

    public GoalProgressResponse() {}

    public GoalProgressResponse(Long id, String name, Double overallProgress, List<IndicatorSummaryResponse> linkedIndicators, String status) {
        this.id = id;
        this.name = name;
        this.overallProgress = overallProgress;
        this.linkedIndicators = linkedIndicators;
        this.status = status;
    }

    // Old getters/setters
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public String getGoalName() { return goalName; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public Double getTotalWeight() { return totalWeight; }
    public void setTotalWeight(Double totalWeight) { this.totalWeight = totalWeight; }
    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }
    public List<IndicatorProgressItem> getIndicatorProgress() { return indicatorProgress; }
    public void setIndicatorProgress(List<IndicatorProgressItem> indicatorProgress) { this.indicatorProgress = indicatorProgress; }

    // New getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getOverallProgress() { return overallProgress; }
    public void setOverallProgress(Double overallProgress) { this.overallProgress = overallProgress; }
    public List<IndicatorSummaryResponse> getLinkedIndicators() { return linkedIndicators; }
    public void setLinkedIndicators(List<IndicatorSummaryResponse> linkedIndicators) { this.linkedIndicators = linkedIndicators; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 