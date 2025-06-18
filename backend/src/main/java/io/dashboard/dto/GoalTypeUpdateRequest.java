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
public class GoalTypeUpdateRequest {
    
    @NotBlank(message = "Goal type name is required")
    @Size(max = 100, message = "Goal type name must not exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Goal type description must not exceed 500 characters")
    private String description;
} 