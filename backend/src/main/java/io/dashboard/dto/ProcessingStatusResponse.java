package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingStatusResponse {
    
    private Long jobId;
    private String status;
    private Double progressPercentage;
    private Long recordsProcessed;
    private Long totalRecords;
    private Long errorCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer estimatedTimeRemainingMinutes;
    private Map<String, Object> currentStep;
} 