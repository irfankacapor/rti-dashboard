package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fact_indicator_values", indexes = {
    @Index(name = "idx_fact_indicator", columnList = "indicator_id"),
    @Index(name = "idx_fact_time", columnList = "time_id"),
    @Index(name = "idx_fact_location", columnList = "location_id"),
    @Index(name = "idx_fact_source_hash", columnList = "source_row_hash"),
    @Index(name = "idx_fact_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactIndicatorValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    private Indicator indicator;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private DimTime time;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private DimLocation location;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subarea_id")
    private Subarea subarea;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "fact_indicator_value_generic",
        joinColumns = @JoinColumn(name = "fact_indicator_value_id"),
        inverseJoinColumns = @JoinColumn(name = "generic_id")
    )
    private List<DimGeneric> generics = new ArrayList<>();
    
    @Column(name = "numeric_value", nullable = false, precision = 19, scale = 6)
    private BigDecimal value;
    
    @Column(name = "source_row_hash", nullable = false, length = 64)
    private String sourceRowHash;
    
    @Column(name = "source_file", length = 255)
    private String sourceFile;
    
    @Column(name = "source_row_number")
    private Integer sourceRowNumber;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "is_aggregated")
    private Boolean isAggregated;
    
    @Column(name = "direction", length = 20)
    private String direction;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 