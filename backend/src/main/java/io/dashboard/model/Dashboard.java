package io.dashboard.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import io.dashboard.model.LayoutType;

@Entity
public class Dashboard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long defaultLocationId;
    private Integer defaultYear;
    @Enumerated(EnumType.STRING)
    private LayoutType layoutType;
    private LocalDateTime createdAt;

    public Dashboard() {}

    public Dashboard(String name, String description, Long defaultLocationId, Integer defaultYear, LayoutType layoutType, LocalDateTime createdAt) {
        this.name = name;
        this.description = description;
        this.defaultLocationId = defaultLocationId;
        this.defaultYear = defaultYear;
        this.layoutType = layoutType;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getDefaultLocationId() { return defaultLocationId; }
    public void setDefaultLocationId(Long defaultLocationId) { this.defaultLocationId = defaultLocationId; }
    public Integer getDefaultYear() { return defaultYear; }
    public void setDefaultYear(Integer defaultYear) { this.defaultYear = defaultYear; }
    public LayoutType getLayoutType() { return layoutType; }
    public void setLayoutType(LayoutType layoutType) { this.layoutType = layoutType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 