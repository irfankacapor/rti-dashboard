package io.dashboard.dto;

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
public class IndicatorValueRow {
    private Long factId; // for updates
    private Map<String, String> dimensions; // {"time": "2025", "location": "Wien", ...}
    private BigDecimal value;
    private boolean isEmpty; // if value is null
} 