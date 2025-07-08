package io.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class IndicatorChartResponse {
    private String indicatorId;
    private String indicatorName;
    private String unit;
    private String aggregationType;
    private List<ChartDataPoint> dataPoints;
    private List<String> availableDimensions;

    @Data
    @Builder
    public static class ChartDataPoint {
        private String label;        // Year, location name, etc.
        private BigDecimal value;    // Aggregated value
        private String dimensionValue; // Raw dimension for sorting
    }
} 