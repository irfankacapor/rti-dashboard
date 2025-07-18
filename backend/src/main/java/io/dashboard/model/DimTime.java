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
    
    @Column(name = "\"value\"")
    private String value; // Original time value from CSV
    
    @Enumerated(EnumType.STRING)
    @Column(name = "time_type")
    private DimensionType timeType; // Type of time dimension
    
    @Column(name = "\"year\"")
    private Integer year;
    
    @Column(name = "\"month\"")
    private Integer month;
    
    @Column(name = "\"day\"")
    private Integer day;
    
    @Column(name = "quarter")
    private Integer quarter;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 