package io.dashboard.dto;

import io.dashboard.model.Direction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvIndicatorData {
    @NotBlank(message = "Indicator name is required")
    private String name;
    
    private String description;
    private String unit;
    private String source;
    
    @NotNull(message = "Subarea ID is required")
    private Long subareaId;
    
    @NotNull(message = "Direction is required")
    private Direction direction;
    
    private Double aggregationWeight = 1.0;
    
    @NotNull(message = "Values list is required")
    @Valid
    private List<IndicatorValue> values;
} 