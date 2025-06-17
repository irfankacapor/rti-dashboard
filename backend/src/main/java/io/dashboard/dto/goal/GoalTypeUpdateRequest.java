package io.dashboard.dto.goal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalTypeUpdateRequest {
    
    @NotBlank(message = "Goal type name is required")
    private String name;
    
    private String description;
} 