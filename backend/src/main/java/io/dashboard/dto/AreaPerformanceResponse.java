package io.dashboard.dto;

import java.util.List;

public class AreaPerformanceResponse {
    private Long id;
    private String name;
    private String description;
    private Double score;
    private String colorCode;
    private List<SubareaPerformanceResponse> subareas;

    public AreaPerformanceResponse() {}

    public AreaPerformanceResponse(Long id, String name, String description, Double score, String colorCode, List<SubareaPerformanceResponse> subareas) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.score = score;
        this.colorCode = colorCode;
        this.subareas = subareas;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public List<SubareaPerformanceResponse> getSubareas() { return subareas; }
    public void setSubareas(List<SubareaPerformanceResponse> subareas) { this.subareas = subareas; }
} 