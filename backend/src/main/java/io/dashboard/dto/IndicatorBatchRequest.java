package io.dashboard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class IndicatorBatchRequest {
    @NotNull(message = "Indicators list cannot be null")
    @NotEmpty(message = "Indicators list cannot be empty")
    @Valid
    private List<CsvIndicatorData> indicators;
} 