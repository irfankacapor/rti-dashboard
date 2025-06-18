package io.dashboard.controller;

import io.dashboard.dto.GoalSubareaLinkResponse;
import io.dashboard.service.GoalSubareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subareas")
@RequiredArgsConstructor
public class SubareaGoalController {
    private final GoalSubareaService goalSubareaService;

    @GetMapping("/{subareaId}/goals")
    public ResponseEntity<List<GoalSubareaLinkResponse>> getGoalsBySubarea(@PathVariable Long subareaId) {
        List<GoalSubareaLinkResponse> responses = goalSubareaService.findGoalsBySubarea(subareaId);
        return ResponseEntity.ok(responses);
    }
} 