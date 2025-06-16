package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "csv_columns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsvColumn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private CsvAnalysis csvAnalysis;
    
    @Column(name = "column_index", nullable = false)
    private Integer columnIndex;
    
    @Column(name = "column_name", nullable = false)
    private String columnName;
    
    @Column(name = "data_type", nullable = false)
    private String dataType;
    
    @Column(name = "sample_values", columnDefinition = "TEXT")
    private String sampleValues; // JSON array of sample values
    
    @Column(name = "null_count")
    private Long nullCount;
    
    @Column(name = "empty_count")
    private Long emptyCount;
    
    @Column(name = "unique_count")
    private Long uniqueCount;
} 