package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnitResponse {
    private Long id;
    private String code;
    private String description;
    private LocalDateTime createdAt;
} 