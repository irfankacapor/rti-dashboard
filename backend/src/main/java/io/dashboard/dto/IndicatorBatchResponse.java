package io.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorBatchResponse {
    private List<IndicatorResponse> createdIndicators;
    private Integer totalFactRecords;
    private String message;
    private List<String> warnings;
} 