package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataQualityReport {
    
    private Long totalRecords;
    private Long validRecords;
    private Long errorRecords;
    private Long warningRecords;
    private Double qualityScore;
    private List<String> warnings;
    private List<String> errors;
    private Map<String, Long> errorTypeCounts;
    private Map<String, Long> columnErrorCounts;
    private Map<String, Object> qualityMetrics;
    private List<String> recommendations;
} 