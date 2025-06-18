package io.dashboard.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalResponse {
    
    private Long id;
    private GoalTypeResponse goalType;
    private String name;
    private String url;
    private Integer year;
    private String description;
    private String attributes;
    private LocalDateTime createdAt;
    private Long targetCount;
} 