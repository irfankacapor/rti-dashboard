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
public class GoalGroupResponse {
    
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private Long goalCount;
} 