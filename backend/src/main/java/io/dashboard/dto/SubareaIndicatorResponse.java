package io.dashboard.dto;

import io.dashboard.model.Direction;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubareaIndicatorResponse {
    private Long subareaId;
    private String subareaCode;
    private String subareaName;
    private Direction direction;
    private Double aggregationWeight;
    private LocalDateTime createdAt;
} 