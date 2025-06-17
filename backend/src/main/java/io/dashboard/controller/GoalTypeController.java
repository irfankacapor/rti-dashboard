package io.dashboard.controller;

import io.dashboard.dto.goal.GoalTypeCreateRequest;
import io.dashboard.dto.goal.GoalTypeResponse;
import io.dashboard.dto.goal.GoalTypeUpdateRequest;
import io.dashboard.service.GoalTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goal-types")
@RequiredArgsConstructor
@Slf4j
public class GoalTypeController {
    
    private final GoalTypeService goalTypeService;
    
    @GetMapping
    public ResponseEntity<List<GoalTypeResponse>> getAllGoalTypes() {
        log.debug("GET /api/v1/goal-types - Getting all goal types");
        List<GoalTypeResponse> goalTypes = goalTypeService.findAll();
        return ResponseEntity.ok(goalTypes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GoalTypeResponse> getGoalTypeById(@PathVariable Long id) {
        log.debug("GET /api/v1/goal-types/{} - Getting goal type by ID", id);
        GoalTypeResponse goalType = goalTypeService.findById(id);
        return ResponseEntity.ok(goalType);
    }
    
    @PostMapping
    public ResponseEntity<GoalTypeResponse> createGoalType(@Valid @RequestBody GoalTypeCreateRequest request) {
        log.debug("POST /api/v1/goal-types - Creating new goal type: {}", request.getName());
        GoalTypeResponse goalType = goalTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(goalType);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GoalTypeResponse> updateGoalType(@PathVariable Long id, 
                                                          @Valid @RequestBody GoalTypeUpdateRequest request) {
        log.debug("PUT /api/v1/goal-types/{} - Updating goal type", id);
        GoalTypeResponse goalType = goalTypeService.update(id, request);
        return ResponseEntity.ok(goalType);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoalType(@PathVariable Long id) {
        log.debug("DELETE /api/v1/goal-types/{} - Deleting goal type", id);
        goalTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 