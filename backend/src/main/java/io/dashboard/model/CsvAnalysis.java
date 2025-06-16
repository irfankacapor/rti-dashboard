package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "csv_analyses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvAnalysis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_id", nullable = false)
    private Long jobId;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "row_count", nullable = false)
    private Long rowCount;
    
    @Column(name = "column_count", nullable = false)
    private Integer columnCount;
    
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers; // JSON array of header names
    
    @Column(name = "delimiter", nullable = false)
    private String delimiter;
    
    @Column(name = "has_header", nullable = false)
    private Boolean hasHeader;
    
    @Column(name = "encoding", nullable = false)
    private String encoding;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @CreationTimestamp
    @Column(name = "analyzed_at", nullable = false, updatable = false)
    private LocalDateTime analyzedAt;
    
    @OneToMany(mappedBy = "csvAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CsvColumn> columns = new ArrayList<>();
} 