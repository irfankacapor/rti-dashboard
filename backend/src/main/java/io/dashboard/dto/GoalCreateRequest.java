package io.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalCreateRequest {
    
    @NotNull(message = "Goal type ID is required")
    private Long goalTypeId;
    
    @NotBlank(message = "Goal name is required")
    @Size(max = 200, message = "Goal name must not exceed 200 characters")
    private String name;
    
    @Size(max = 500, message = "Goal URL must not exceed 500 characters")
    private String url;
    
    @NotNull(message = "Goal year is required")
    private Integer year;
    
    @Size(max = 1000, message = "Goal description must not exceed 1000 characters")
    private String description;
    
    private String attributes;
} 