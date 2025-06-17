package io.dashboard.dto.goal;

import io.dashboard.enums.ImpactDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorProgressInfo {
    
    private Long indicatorId;
    private String indicatorName;
    private BigDecimal currentValue;
    private BigDecimal targetValue;
    private BigDecimal progress;
    private BigDecimal weight;
    private ImpactDirection impactDirection;
    private String unit;
} 