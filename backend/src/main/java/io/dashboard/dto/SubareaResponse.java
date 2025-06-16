package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubareaResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long areaId;
    private String areaName;
    private int indicatorCount;
} 