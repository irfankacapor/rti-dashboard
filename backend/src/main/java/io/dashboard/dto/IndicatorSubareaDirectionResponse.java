package io.dashboard.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorSubareaDirectionResponse {
    private Long subareaId;
    private String subareaName;
    private String direction;
    private Long valueCount;
} 