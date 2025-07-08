package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.model.ImpactDirection;
import io.dashboard.service.GoalIndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalIndicatorController {
    private final GoalIndicatorService goalIndicatorService;

    @PostMapping("/{goalId}/indicators/{indicatorId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalIndicatorResponse> linkGoalToIndicator(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @RequestBody @Valid GoalIndicatorLinkRequest request) {
        GoalIndicatorResponse response = goalIndicatorService.linkGoalToIndicator(
                goalId, indicatorId, request.getAggregationWeight(), request.getImpactDirection());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{goalId}/indicators/{indicatorId}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> unlinkGoalFromIndicator(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId) {
        goalIndicatorService.unlinkGoalFromIndicator(goalId, indicatorId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{goalId}/indicators/{indicatorId}/weight")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalIndicatorResponse> updateGoalIndicatorWeight(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @RequestBody @Valid GoalIndicatorUpdateRequest request) {
        GoalIndicatorResponse response = goalIndicatorService.updateGoalIndicatorWeight(
                goalId, indicatorId, request.getAggregationWeight());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{goalId}/indicators/{indicatorId}/direction")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<GoalIndicatorResponse> updateGoalIndicatorDirection(
            @PathVariable Long goalId,
            @PathVariable Long indicatorId,
            @RequestBody @Valid GoalIndicatorUpdateRequest request) {
        GoalIndicatorResponse response = goalIndicatorService.updateGoalIndicatorDirection(
                goalId, indicatorId, request.getImpactDirection());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{goalId}/indicators")
    @PermitAll
    public ResponseEntity<List<GoalIndicatorResponse>> getIndicatorsByGoal(@PathVariable Long goalId) {
        List<GoalIndicatorResponse> responses = goalIndicatorService.findIndicatorsByGoal(goalId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/indicators/{indicatorId}/goals")
    @PermitAll
    public ResponseEntity<List<GoalIndicatorResponse>> getGoalsByIndicator(@PathVariable Long indicatorId) {
        List<GoalIndicatorResponse> responses = goalIndicatorService.findGoalsByIndicator(indicatorId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{goalId}/indicators/bulk")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<List<GoalIndicatorResponse>> bulkLinkIndicators(
            @PathVariable Long goalId,
            @RequestBody @Valid BulkGoalIndicatorRequest request) {
        List<GoalIndicatorResponse> responses = goalIndicatorService.bulkLinkIndicators(goalId, request.getLinks());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/{goalId}/progress")
    @PermitAll
    public ResponseEntity<GoalProgressResponse> getGoalProgress(@PathVariable Long goalId) {
        GoalProgressResponse response = goalIndicatorService.calculateGoalProgress(goalId);
        return ResponseEntity.ok(response);
    }
} 