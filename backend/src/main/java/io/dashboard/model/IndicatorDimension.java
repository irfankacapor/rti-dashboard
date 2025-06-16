package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "indicator_dimensions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicatorDimension {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_type", nullable = false, length = 20)
    private DimensionType dimensionType;
    
    @Column(name = "column_header", length = 100)
    private String columnHeader;
    
    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    
    @Column(name = "mapping_rules", columnDefinition = "TEXT")
    private String mappingRules; // JSON string for complex mapping rules
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 