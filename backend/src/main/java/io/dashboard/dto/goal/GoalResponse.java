package io.dashboard.dto.goal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    
    private Long id;
    private String name;
    private String description;
    private String url;
    private Integer year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private GoalTypeResponse goalType;
    private List<GoalTargetResponse> targets;
    private List<GoalIndicatorResponse> indicators;
} 