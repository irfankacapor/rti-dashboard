package io.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PerformanceScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long subareaId;
    private Double score;
    private String colorCode;
    private LocalDateTime calculatedAt;
    private String basedOnIndicators; // Comma-separated indicator IDs

    public PerformanceScore() {}

    public PerformanceScore(Long subareaId, Double score, String colorCode, LocalDateTime calculatedAt, String basedOnIndicators) {
        this.subareaId = subareaId;
        this.score = score;
        this.colorCode = colorCode;
        this.calculatedAt = calculatedAt;
        this.basedOnIndicators = basedOnIndicators;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSubareaId() { return subareaId; }
    public void setSubareaId(Long subareaId) { this.subareaId = subareaId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public String getBasedOnIndicators() { return basedOnIndicators; }
    public void setBasedOnIndicators(String basedOnIndicators) { this.basedOnIndicators = basedOnIndicators; }
} 