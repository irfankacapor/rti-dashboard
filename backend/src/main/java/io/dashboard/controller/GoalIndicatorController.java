package io.dashboard.controller;

import io.dashboard.dto.goal.*;
import io.dashboard.enums.ImpactDirection;
import io.dashboard.service.GoalIndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class GoalIndicatorController {
    
    private final GoalIndicatorService goalIndicatorService;
    
    @PostMapping("/goals/{goalId}/indicators/{indicatorId}")
    public ResponseEntity<GoalIndicatorResponse> linkGoalToIndicator(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @Valid @RequestBody GoalIndicatorLinkRequest request) {
        
        log.debug("POST /api/v1/goals/{}/indicators/{} - Linking goal to indicator", goalId, indicatorId);
        
        GoalIndicatorResponse response = goalIndicatorService.linkGoalToIndicator(
                goalId, indicatorId, request.getWeight(), request.getImpactDirection());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @DeleteMapping("/goals/{goalId}/indicators/{indicatorId}")
    public ResponseEntity<Void> unlinkGoalFromIndicator(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId) {
        
        log.debug("DELETE /api/v1/goals/{}/indicators/{} - Unlinking goal from indicator", goalId, indicatorId);
        
        goalIndicatorService.unlinkGoalFromIndicator(goalId, indicatorId);
        
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/goals/{goalId}/indicators/{indicatorId}/weight")
    public ResponseEntity<GoalIndicatorResponse> updateGoalIndicatorWeight(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @Valid @RequestBody GoalIndicatorUpdateRequest request) {
        
        log.debug("PUT /api/v1/goals/{}/indicators/{}/weight - Updating goal indicator weight", goalId, indicatorId);
        
        if (request.getWeight() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        GoalIndicatorResponse response = goalIndicatorService.updateGoalIndicatorWeight(goalId, indicatorId, request.getWeight());
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/goals/{goalId}/indicators/{indicatorId}/impact")
    public ResponseEntity<GoalIndicatorResponse> updateImpactDirection(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @Valid @RequestBody GoalIndicatorUpdateRequest request) {
        
        log.debug("PUT /api/v1/goals/{}/indicators/{}/impact - Updating impact direction", goalId, indicatorId);
        
        if (request.getImpactDirection() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        GoalIndicatorResponse response = goalIndicatorService.updateImpactDirection(goalId, indicatorId, request.getImpactDirection());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/goals/{goalId}/indicators")
    public ResponseEntity<List<GoalIndicatorResponse>> getIndicatorsByGoal(@PathVariable Long goalId) {
        log.debug("GET /api/v1/goals/{}/indicators - Getting indicators for goal", goalId);
        
        List<GoalIndicatorResponse> response = goalIndicatorService.findIndicatorsByGoal(goalId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/indicators/{indicatorId}/goals")
    public ResponseEntity<List<GoalIndicatorResponse>> getGoalsByIndicator(@PathVariable Long indicatorId) {
        log.debug("GET /api/v1/indicators/{}/goals - Getting goals for indicator", indicatorId);
        
        List<GoalIndicatorResponse> response = goalIndicatorService.findGoalsByIndicator(indicatorId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/goals/{goalId}/indicators/bulk")
    public ResponseEntity<List<GoalIndicatorResponse>> bulkLinkIndicators(
            @PathVariable Long goalId,
            @Valid @RequestBody BulkGoalIndicatorRequest request) {
        
        log.debug("POST /api/v1/goals/{}/indicators/bulk - Bulk linking indicators to goal", goalId);
        
        List<GoalIndicatorResponse> response = goalIndicatorService.bulkLinkIndicators(goalId, request.getIndicators());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/goals/{goalId}/progress")
    public ResponseEntity<GoalProgressResponse> getGoalProgress(@PathVariable Long goalId) {
        log.debug("GET /api/v1/goals/{}/progress - Calculating goal progress", goalId);
        
        GoalProgressResponse response = goalIndicatorService.calculateGoalProgress(goalId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/goals/{goalId}/indicators/{indicatorId}")
    public ResponseEntity<Boolean> validateGoalIndicatorLink(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId) {
        
        log.debug("GET /api/v1/goals/{}/indicators/{} - Validating goal indicator link", goalId, indicatorId);
        
        boolean exists = goalIndicatorService.validateGoalIndicatorLink(goalId, indicatorId);
        
        return ResponseEntity.ok(exists);
    }
} 