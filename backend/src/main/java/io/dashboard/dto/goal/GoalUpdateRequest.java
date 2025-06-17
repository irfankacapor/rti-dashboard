package io.dashboard.dto.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalUpdateRequest {
    
    @NotBlank(message = "Goal name is required")
    private String name;
    
    private String description;
    
    @Pattern(regexp = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?$", 
             message = "URL must be a valid URL format")
    private String url;
    
    @NotNull(message = "Goal year is required")
    private Integer year;
    
    @NotNull(message = "Goal type ID is required")
    private Long goalTypeId;
    
    private String attributes;
} 