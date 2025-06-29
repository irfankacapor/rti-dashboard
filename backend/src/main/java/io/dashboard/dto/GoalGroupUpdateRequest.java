package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalGroupUpdateRequest {
    
    @NotBlank(message = "Goal group name is required")
    @Size(max = 100, message = "Goal group name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Goal group description must not exceed 500 characters")
    private String description;
} 