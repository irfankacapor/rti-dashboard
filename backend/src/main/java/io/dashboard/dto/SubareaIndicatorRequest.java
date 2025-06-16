package io.dashboard.dto;

import io.dashboard.model.Direction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubareaIndicatorRequest {
    @NotNull
    private Direction direction;
    
    private Double aggregationWeight;
} 