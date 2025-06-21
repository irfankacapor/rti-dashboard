package io.dashboard.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorValue {
    @NotNull(message = "Value is required")
    private BigDecimal value;
    
    // Dimensional context
    private String timeValue; // e.g., "2023"
    private String timeType; // "year", "month", "day"
    
    private String locationValue; // e.g., "Burgenland"
    private String locationType; // "state", "country", "city"
    
    // Custom dimensions (generic)
    private Map<String, String> customDimensions; // e.g., {"Sector": "Public", "Gender": "Male"}
} 