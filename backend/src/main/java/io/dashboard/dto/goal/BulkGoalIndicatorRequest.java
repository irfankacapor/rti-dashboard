package io.dashboard.dto.goal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGoalIndicatorRequest {
    
    @NotNull(message = "Indicators list is required")
    @NotEmpty(message = "Indicators list cannot be empty")
    @Valid
    private List<GoalIndicatorLinkRequest> indicators;
} 