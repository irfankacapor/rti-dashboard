package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "dim_generic")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimGeneric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "dimension_name", length = 100)
    private String dimensionName; // Name of the dimension (e.g., "Category", "Sector")
    
    @Column(name = "\"value\"", nullable = false, length = 255)
    private String value;
    
    @Column(name = "category", length = 50)
    private String category; // For grouping related dimensions
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 