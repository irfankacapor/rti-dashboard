package io.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorValuesResponse {
    private List<IndicatorValueRow> rows;
    private List<String> dimensionColumns; // ["time", "location", "gender", ...]
    private String indicatorName;
    private String dataType;
} 