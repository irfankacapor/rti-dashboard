package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "dim_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimTime {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @Column(name = "month")
    private Integer month;
    
    @Column(name = "day")
    private Integer day;
    
    @Column(name = "quarter")
    private Integer quarter;
    
    @Column(name = "time_period", length = 50)
    private String timePeriod; // e.g., "Q1 2022", "Jan 2021"
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 