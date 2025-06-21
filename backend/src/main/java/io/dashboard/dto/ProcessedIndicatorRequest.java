package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProcessedIndicatorRequest {
    @NotBlank(message = "Indicator name is required")
    private String name;
    
    @NotNull(message = "Dimensions list is required")
    private List<String> dimensions;
    
    @NotNull(message = "Value count is required")
    private Integer valueCount;
    
    private String unit;
    private String source;
    private String subareaId;
    private String direction; // 'input' or 'output'
} 