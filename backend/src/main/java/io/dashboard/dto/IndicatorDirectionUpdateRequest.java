package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IndicatorDirectionUpdateRequest {
    @NotBlank(message = "Direction is required")
    private String direction;
} 