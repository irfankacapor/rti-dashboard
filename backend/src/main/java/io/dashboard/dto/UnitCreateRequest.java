package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UnitCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String code;

    @Size(max = 255)
    private String description;
} 