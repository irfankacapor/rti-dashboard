package io.dashboard.controller;

import io.dashboard.dto.GoalTargetCreateRequest;
import io.dashboard.dto.GoalTargetResponse;
import io.dashboard.dto.GoalTargetUpdateRequest;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalTarget;
import io.dashboard.model.Indicator;
import io.dashboard.service.GoalTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goal-targets")
@Slf4j
public class GoalTargetController {
    
    private final GoalTargetService goalTargetService;
    
    @GetMapping
    @PermitAll
    public ResponseEntity<List<GoalTargetResponse>> getAllTargets() {
        log.debug("Getting all goal targets");
        // This would need a new method in service to get all targets
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<GoalTargetResponse> getTargetById(@PathVariable Long id) {
        log.debug("Getting goal target by ID: {}", id);
        // This would need a new method in service to get target by ID
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/goals/{goalId}/targets")
    @PermitAll
    public ResponseEntity<List<GoalTargetResponse>> getTargetsForGoal(@PathVariable Long goalId) {
        log.debug("Getting targets for goal ID: {}", goalId);
        List<GoalTargetResponse> targets = goalTargetService.findByGoalId(goalId);
        return ResponseEntity.ok(targets);
    }
    
    @GetMapping("/indicators/{indicatorId}/targets")
    @PermitAll
    public ResponseEntity<List<GoalTargetResponse>> getTargetsForIndicator(@PathVariable Long indicatorId) {
        log.debug("Getting targets for indicator ID: {}", indicatorId);
        List<GoalTargetResponse> targets = goalTargetService.findByIndicatorId(indicatorId);
        return ResponseEntity.ok(targets);
    }
    
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalTargetResponse> createTarget(@Valid @RequestBody GoalTargetCreateRequest request) {
        log.debug("Creating new goal target for goal ID: {} and indicator ID: {}", 
                request.getGoalId(), request.getIndicatorId());
        
        Goal goal = Goal.builder().id(request.getGoalId()).build();
        Indicator indicator = Indicator.builder().id(request.getIndicatorId()).build();
        
        GoalTarget target = GoalTarget.builder()
                .goal(goal)
                .indicator(indicator)
                .targetYear(request.getTargetYear())
                .targetValue(request.getTargetValue())
                .targetType(request.getTargetType())
                .targetPercentage(request.getTargetPercentage())
                .build();
        
        GoalTargetResponse response = goalTargetService.create(target);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalTargetResponse> updateTarget(@PathVariable Long id, 
                                                          @Valid @RequestBody GoalTargetUpdateRequest request) {
        log.debug("Updating goal target with ID: {}", id);
        
        GoalTarget target = GoalTarget.builder()
                .targetYear(request.getTargetYear())
                .targetValue(request.getTargetValue())
                .targetType(request.getTargetType())
                .targetPercentage(request.getTargetPercentage())
                .build();
        
        GoalTargetResponse response = goalTargetService.update(id, target);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteTarget(@PathVariable Long id) {
        log.debug("Deleting goal target with ID: {}", id);
        goalTargetService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 