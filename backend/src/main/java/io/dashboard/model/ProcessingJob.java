package io.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "upload_job_id", nullable = false)
    private Long uploadJobId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcessingStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @Column(name = "records_processed")
    private Long recordsProcessed;
    
    @Column(name = "error_count")
    private Long errorCount;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "progress_percentage")
    private Double progressPercentage;
    
    @Column(name = "batch_size")
    private Integer batchSize;
    
    @Column(name = "total_records")
    private Long totalRecords;
    
    @Column(name = "processing_config", columnDefinition = "TEXT")
    private String processingConfig; // JSON configuration
    
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