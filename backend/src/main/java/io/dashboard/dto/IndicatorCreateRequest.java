package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IndicatorCreateRequest {
    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private Boolean isComposite = false;

    private String unit;
    private String unitPrefix;
    private String unitSuffix;

    private Long dataTypeId;
} 