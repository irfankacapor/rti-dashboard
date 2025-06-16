package io.dashboard.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitUpdateRequest {
    @Size(max = 255)
    private String description;
} 