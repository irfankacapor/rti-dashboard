package io.dashboard.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataTypeResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime createdAt;
} 