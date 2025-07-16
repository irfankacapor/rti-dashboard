package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IndicatorUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private Boolean isComposite;

    private Long unitId;
    private String unitPrefix;
    private String unitSuffix;

    private Long subareaId;
    private String direction;
    private Double aggregationWeight;

    private Long dataTypeId;
} 