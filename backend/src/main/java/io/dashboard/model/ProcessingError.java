package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_errors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingError {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processing_job_id", nullable = false)
    private ProcessingJob processingJob;
    
    @Column(name = "row_number")
    private Integer rowNumber;
    
    @Column(name = "column_name", length = 255)
    private String columnName;
    
    @Column(name = "error_message", columnDefinition = "TEXT", nullable = false)
    private String errorMessage;
    
    @Column(name = "raw_value", columnDefinition = "TEXT")
    private String rawValue;
    
    @Column(name = "error_type", length = 100)
    private String errorType;
    
    @Column(name = "severity", length = 20)
    private String severity; // ERROR, WARNING, INFO
    
    @Column(name = "is_resolved")
    private Boolean isResolved;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
} 