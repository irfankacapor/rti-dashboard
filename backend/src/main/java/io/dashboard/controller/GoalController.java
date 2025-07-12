package io.dashboard.controller;

import io.dashboard.dto.GoalCreateRequest;
import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalTargetResponse;
import io.dashboard.dto.GoalUpdateRequest;
import io.dashboard.service.GoalService;
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
@RequestMapping("/api/v1/goals")
@Slf4j
public class GoalController {
    
    private final GoalService goalService;
    private final GoalTargetService goalTargetService;
    
    @GetMapping
    @PermitAll
    public ResponseEntity<List<GoalResponse>> getAllGoals() {
        log.debug("Getting all goals");
        List<GoalResponse> goals = goalService.findAll();
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        log.debug("Getting goal by ID: {}", id);
        GoalResponse goal = goalService.findById(id);
        return ResponseEntity.ok(goal);
    }
    
    @GetMapping("/goal-groups/{groupId}/goals")
    @PermitAll
    public ResponseEntity<List<GoalResponse>> getGoalsByGroup(@PathVariable Long groupId) {
        log.debug("Getting goals by group ID: {}", groupId);
        List<GoalResponse> goals = goalService.findByGoalGroupId(groupId);
        return ResponseEntity.ok(goals);
    }
    
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalCreateRequest request) {
        log.debug("Creating new goal: {}", request.getName());
        GoalResponse response = goalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, 
                                                  @Valid @RequestBody GoalUpdateRequest request) {
        log.debug("Updating goal with ID: {}", id);
        GoalResponse response = goalService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        log.debug("Deleting goal with ID: {}", id);
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/targets")
    @PermitAll
    public ResponseEntity<List<GoalTargetResponse>> getTargetsForGoal(@PathVariable Long id) {
        log.debug("Getting targets for goal ID: {}", id);
        List<GoalTargetResponse> targets = goalTargetService.findByGoalId(id);
        return ResponseEntity.ok(targets);
    }
} 