package io.dashboard.controller;

import io.dashboard.dto.GoalSubareaLinkResponse;
import io.dashboard.service.GoalSubareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
@RequiredArgsConstructor
public class GoalSubareaController {
    private final GoalSubareaService goalSubareaService;

    @PostMapping("/{goalId}/subareas/{subareaId}")
    public ResponseEntity<GoalSubareaLinkResponse> linkGoalToSubarea(
            @PathVariable Long goalId,
            @PathVariable Long subareaId) {
        GoalSubareaLinkResponse response = goalSubareaService.linkGoalToSubarea(goalId, subareaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{goalId}/subareas/{subareaId}")
    public ResponseEntity<Void> unlinkGoalFromSubarea(
            @PathVariable Long goalId,
            @PathVariable Long subareaId) {
        goalSubareaService.unlinkGoalFromSubarea(goalId, subareaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{goalId}/subareas")
    public ResponseEntity<List<GoalSubareaLinkResponse>> getSubareasByGoal(@PathVariable Long goalId) {
        List<GoalSubareaLinkResponse> responses = goalSubareaService.findSubareasByGoal(goalId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/subareas/{subareaId}/goals")
    public ResponseEntity<List<GoalSubareaLinkResponse>> getGoalsBySubarea(@PathVariable Long subareaId) {
        List<GoalSubareaLinkResponse> responses = goalSubareaService.findGoalsBySubarea(subareaId);
        return ResponseEntity.ok(responses);
    }
} 