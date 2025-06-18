package io.dashboard.controller;

import io.dashboard.dto.GoalTypeCreateRequest;
import io.dashboard.dto.GoalTypeResponse;
import io.dashboard.dto.GoalTypeUpdateRequest;
import io.dashboard.model.GoalType;
import io.dashboard.service.GoalTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/goal-types")
@Slf4j
public class GoalTypeController {
    
    private final GoalTypeService goalTypeService;
    
    @GetMapping
    public ResponseEntity<List<GoalTypeResponse>> getAllGoalTypes() {
        log.debug("Getting all goal types");
        List<GoalTypeResponse> goalTypes = goalTypeService.findAll();
        return ResponseEntity.ok(goalTypes);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GoalTypeResponse> getGoalTypeById(@PathVariable Long id) {
        log.debug("Getting goal type by ID: {}", id);
        GoalTypeResponse goalType = goalTypeService.findById(id);
        return ResponseEntity.ok(goalType);
    }
    
    @PostMapping
    public ResponseEntity<GoalTypeResponse> createGoalType(@Valid @RequestBody GoalTypeCreateRequest request) {
        log.debug("Creating new goal type: {}", request.getName());
        
        GoalType goalType = GoalType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        GoalTypeResponse response = goalTypeService.create(goalType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<GoalTypeResponse> updateGoalType(@PathVariable Long id, 
                                                          @Valid @RequestBody GoalTypeUpdateRequest request) {
        log.debug("Updating goal type with ID: {}", id);
        
        GoalType goalType = GoalType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        GoalTypeResponse response = goalTypeService.update(id, goalType);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoalType(@PathVariable Long id) {
        log.debug("Deleting goal type with ID: {}", id);
        goalTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 