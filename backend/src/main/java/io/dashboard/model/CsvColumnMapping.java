package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "csv_column_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvColumnMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private CsvAnalysis analysis;
    
    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "dimension_type", nullable = false, length = 20)
    private DimensionType dimensionType;
    
    @Column(name = "column_header", length = 100)
    private String columnHeader;
    
    @Column(name = "mapping_rules", columnDefinition = "TEXT")
    private String mappingRules; // JSON string for mapping rules
    
    @Column(name = "confidence_score")
    private Double confidenceScore; // 0.0 to 1.0 for auto-detected mappings
    
    @Column(name = "is_auto_detected")
    private Boolean isAutoDetected = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 