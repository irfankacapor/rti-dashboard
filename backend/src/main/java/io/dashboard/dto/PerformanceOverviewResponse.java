package io.dashboard.dto;

import java.util.Map;

public class PerformanceOverviewResponse {
    private Integer totalAreas;
    private Integer totalSubareas;
    private Double averageScore;
    private Map<String, Integer> colorDistribution;

    public PerformanceOverviewResponse() {}

    public PerformanceOverviewResponse(Integer totalAreas, Integer totalSubareas, Double averageScore, Map<String, Integer> colorDistribution) {
        this.totalAreas = totalAreas;
        this.totalSubareas = totalSubareas;
        this.averageScore = averageScore;
        this.colorDistribution = colorDistribution;
    }

    // Getters and setters
    public Integer getTotalAreas() { return totalAreas; }
    public void setTotalAreas(Integer totalAreas) { this.totalAreas = totalAreas; }
    public Integer getTotalSubareas() { return totalSubareas; }
    public void setTotalSubareas(Integer totalSubareas) { this.totalSubareas = totalSubareas; }
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    public Map<String, Integer> getColorDistribution() { return colorDistribution; }
    public void setColorDistribution(Map<String, Integer> colorDistribution) { this.colorDistribution = colorDistribution; }
} 