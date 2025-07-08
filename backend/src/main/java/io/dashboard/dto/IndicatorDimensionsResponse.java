package io.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IndicatorDimensionsResponse {
    private String indicatorId;
    private List<DimensionInfo> availableDimensions;

    @Data
    @Builder
    public static class DimensionInfo {
        private String type;         // "time", "location", "sector", etc.
        private String displayName;  // "Time", "Location", "Sector", etc.
        private List<String> values; // Available values for this dimension
    }
} 