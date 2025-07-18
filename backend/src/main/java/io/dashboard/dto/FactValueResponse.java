package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactValueResponse {
    
    private Long id;
    private Long indicatorId;
    private String indicatorName;
    private Long timeId;
    private String timeValue;
    private Long locationId;
    private String locationName;
    private Long genericId;
    private String genericValue;
    private BigDecimal value;
    private String sourceFile;
    private LocalDateTime createdAt;
    private Map<String, Object> dimensions;
} 