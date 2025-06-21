package io.dashboard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchIndicatorsRequest {
    @NotEmpty(message = "Indicators list cannot be empty")
    @Valid
    private List<ProcessedIndicatorRequest> indicators;
} 