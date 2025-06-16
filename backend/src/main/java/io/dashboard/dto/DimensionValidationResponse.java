package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionValidationResponse {
    
    private Boolean isValid;
    private List<String> errors;
    private List<String> warnings;
    private List<String> suggestions;
    private Integer totalMappings;
    private Integer requiredMappings;
    private Integer missingMappings;
} 