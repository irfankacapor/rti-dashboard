package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IndicatorResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isComposite;
    private LocalDateTime createdAt;
    private UnitResponse unit;
    private DataTypeResponse dataType;
    private List<SubareaIndicatorResponse> subareaIndicators;
} 