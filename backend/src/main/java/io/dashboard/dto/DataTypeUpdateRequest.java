package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DataTypeUpdateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
} 