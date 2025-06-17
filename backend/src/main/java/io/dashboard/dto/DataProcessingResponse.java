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
public class DataProcessingResponse {
    
    private Long processingJobId;
    private String status;
    private String message;
    private Integer estimatedDurationMinutes;
    private LocalDateTime startedAt;
    private Long totalRecords;
    private Integer batchSize;
    private Map<String, Object> processingConfig;
} 