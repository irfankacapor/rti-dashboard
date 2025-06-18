package io.dashboard.controller;

import io.dashboard.dto.GoalCreateRequest;
import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalTargetResponse;
import io.dashboard.dto.GoalUpdateRequest;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalType;
import io.dashboard.service.GoalService;
import io.dashboard.service.GoalTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goals")
@Slf4j
public class GoalController {
    
    private final GoalService goalService;
    private final GoalTargetService goalTargetService;
    
    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAllGoals() {
        log.debug("Getting all goals");
        List<GoalResponse> goals = goalService.findAll();
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        log.debug("Getting goal by ID: {}", id);
        GoalResponse goal = goalService.findById(id);
        return ResponseEntity.ok(goal);
    }
    
    @GetMapping("/goal-types/{typeId}/goals")
    public ResponseEntity<List<GoalResponse>> getGoalsByType(@PathVariable Long typeId) {
        log.debug("Getting goals by type ID: {}", typeId);
        List<GoalResponse> goals = goalService.findByGoalTypeId(typeId);
        return ResponseEntity.ok(goals);
    }
    
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalCreateRequest request) {
        log.debug("Creating new goal: {}", request.getName());
        
        GoalType goalType = GoalType.builder().id(request.getGoalTypeId()).build();
        
        Goal goal = Goal.builder()
                .goalType(goalType)
                .name(request.getName())
                .url(request.getUrl())
                .year(request.getYear())
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        
        GoalResponse response = goalService.create(goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, 
                                                  @Valid @RequestBody GoalUpdateRequest request) {
        log.debug("Updating goal with ID: {}", id);
        
        GoalType goalType = GoalType.builder().id(request.getGoalTypeId()).build();
        
        Goal goal = Goal.builder()
                .goalType(goalType)
                .name(request.getName())
                .url(request.getUrl())
                .year(request.getYear())
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        
        GoalResponse response = goalService.update(id, goal);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        log.debug("Deleting goal with ID: {}", id);
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/targets")
    public ResponseEntity<List<GoalTargetResponse>> getTargetsForGoal(@PathVariable Long id) {
        log.debug("Getting targets for goal ID: {}", id);
        List<GoalTargetResponse> targets = goalTargetService.findByGoalId(id);
        return ResponseEntity.ok(targets);
    }
} 