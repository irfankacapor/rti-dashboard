package io.dashboard.controller;

import io.dashboard.dto.GoalGroupCreateRequest;
import io.dashboard.dto.GoalGroupResponse;
import io.dashboard.dto.GoalGroupUpdateRequest;
import io.dashboard.model.GoalGroup;
import io.dashboard.service.GoalGroupService;
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
@RequestMapping("/api/v1/goal-groups")
@Slf4j
public class GoalGroupController {
    
    private final GoalGroupService goalGroupService;
    
    @GetMapping
    @PermitAll
    public ResponseEntity<List<GoalGroupResponse>> getAllGoalGroups() {
        log.debug("Getting all goal groups");
        List<GoalGroupResponse> goalGroups = goalGroupService.findAll();
        return ResponseEntity.ok(goalGroups);
    }
    
    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<GoalGroupResponse> getGoalGroupById(@PathVariable Long id) {
        log.debug("Getting goal group by ID: {}", id);
        GoalGroupResponse goalGroup = goalGroupService.findById(id);
        return ResponseEntity.ok(goalGroup);
    }
    
    @PostMapping
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalGroupResponse> createGoalGroup(@Valid @RequestBody GoalGroupCreateRequest request) {
        log.debug("Creating new goal group: {}", request.getName());
        
        GoalGroup goalGroup = GoalGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        GoalGroupResponse response = goalGroupService.create(goalGroup);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalGroupResponse> updateGoalGroup(@PathVariable Long id, 
                                                          @Valid @RequestBody GoalGroupUpdateRequest request) {
        log.debug("Updating goal group with ID: {}", id);
        
        GoalGroup goalGroup = GoalGroup.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        GoalGroupResponse response = goalGroupService.update(id, goalGroup);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteGoalGroup(@PathVariable Long id) {
        log.debug("Deleting goal group with ID: {}", id);
        goalGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 