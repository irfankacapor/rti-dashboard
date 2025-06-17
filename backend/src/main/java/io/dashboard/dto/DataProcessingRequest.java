package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataProcessingRequest {
    
    private Long uploadJobId;
    private Boolean overwriteExisting;
    private Map<String, Object> validationRules;
    private Integer batchSize;
    private Boolean enableDataQualityChecks;
    private Boolean enableAggregations;
    private Map<String, Object> processingOptions;
    
    // Default values
    public static DataProcessingRequest defaultRequest(Long uploadJobId) {
        return DataProcessingRequest.builder()
                .uploadJobId(uploadJobId)
                .overwriteExisting(false)
                .batchSize(1000)
                .enableDataQualityChecks(true)
                .enableAggregations(false)
                .build();
    }
} 