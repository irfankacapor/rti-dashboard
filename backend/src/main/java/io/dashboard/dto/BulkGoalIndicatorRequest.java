package io.dashboard.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkGoalIndicatorRequest {
    
    @NotNull(message = "Links list is required")
    @NotEmpty(message = "Links list cannot be empty")
    @Valid
    private List<GoalIndicatorLinkRequest> links;
} 