package io.dashboard.dto;

public class ColorThresholdResponse {
    private Long id;
    private Double minValue;
    private Double maxValue;
    private String colorCode;
    private String description;

    public ColorThresholdResponse() {}

    public ColorThresholdResponse(Long id, Double minValue, Double maxValue, String colorCode, String description) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.colorCode = colorCode;
        this.description = description;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }
    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
} 