package io.dashboard.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalUpdateRequest {
    
    private Long goalGroupId;
    
    @NotNull(message = "Goal type is required")
    private String type; // 'quantitative' or 'qualitative'
    
    @NotBlank(message = "Goal name is required")
    @Size(max = 200, message = "Goal name must not exceed 200 characters")
    private String name;
    
    @Size(max = 500, message = "Goal URL must not exceed 500 characters")
    private String url;
    
    @NotNull(message = "Goal year is required")
    private Integer year;
    
    @Size(max = 1000, message = "Goal description must not exceed 1000 characters")
    private String description;
    
    // List of indicator IDs to link to this goal
    private List<Long> indicators;
} 