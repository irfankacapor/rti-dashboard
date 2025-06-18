package io.dashboard.dto;

public class SubareaPerformanceResponse {
    private Long id;
    private String name;
    private String description;
    private Double score;
    private String colorCode;
    private Integer indicatorCount;
    private Integer goalCount;

    public SubareaPerformanceResponse() {}

    public SubareaPerformanceResponse(Long id, String name, String description, Double score, String colorCode, Integer indicatorCount, Integer goalCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.score = score;
        this.colorCode = colorCode;
        this.indicatorCount = indicatorCount;
        this.goalCount = goalCount;
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
    public Integer getIndicatorCount() { return indicatorCount; }
    public void setIndicatorCount(Integer indicatorCount) { this.indicatorCount = indicatorCount; }
    public Integer getGoalCount() { return goalCount; }
    public void setGoalCount(Integer goalCount) { this.goalCount = goalCount; }
} 