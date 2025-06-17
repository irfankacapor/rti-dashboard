package io.dashboard.controller;

import io.dashboard.dto.goal.*;
import io.dashboard.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
@Slf4j
public class GoalController {
    
    private final GoalService goalService;
    
    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAllGoals() {
        log.debug("GET /api/v1/goals - Getting all goals");
        List<GoalResponse> goals = goalService.findAll();
        return ResponseEntity.ok(goals);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id) {
        log.debug("GET /api/v1/goals/{} - Getting goal by ID", id);
        GoalResponse goal = goalService.findById(id);
        return ResponseEntity.ok(goal);
    }
    
    @GetMapping("/goal-types/{typeId}/goals")
    public ResponseEntity<List<GoalResponse>> getGoalsByType(@PathVariable Long typeId) {
        log.debug("GET /api/v1/goal-types/{}/goals - Getting goals by type", typeId);
        List<GoalResponse> goals = goalService.findByType(typeId);
        return ResponseEntity.ok(goals);
    }
    
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalCreateRequest request) {
        log.debug("POST /api/v1/goals - Creating new goal: {}", request.getName());
        GoalResponse goal = goalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goal);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id, 
                                                  @Valid @RequestBody GoalUpdateRequest request) {
        log.debug("PUT /api/v1/goals/{} - Updating goal", id);
        GoalResponse goal = goalService.update(id, request);
        return ResponseEntity.ok(goal);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        log.debug("DELETE /api/v1/goals/{} - Deleting goal", id);
        goalService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/targets")
    public ResponseEntity<GoalTargetResponse> addTarget(@PathVariable Long id, 
                                                       @Valid @RequestBody GoalTargetRequest request) {
        log.debug("POST /api/v1/goals/{}/targets - Adding target to goal", id);
        GoalTargetResponse target = goalService.addTarget(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(target);
    }
    
    @DeleteMapping("/{id}/targets/{targetId}")
    public ResponseEntity<Void> removeTarget(@PathVariable Long id, @PathVariable Long targetId) {
        log.debug("DELETE /api/v1/goals/{}/targets/{} - Removing target from goal", id, targetId);
        goalService.removeTarget(id, targetId);
        return ResponseEntity.noContent().build();
    }
} 