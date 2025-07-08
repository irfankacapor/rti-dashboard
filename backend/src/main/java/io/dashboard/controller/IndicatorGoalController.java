package io.dashboard.controller;

import io.dashboard.dto.GoalIndicatorResponse;
import io.dashboard.service.GoalIndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.annotation.security.PermitAll;

@RestController
@RequestMapping("/api/v1/indicators")
@RequiredArgsConstructor
public class IndicatorGoalController {
    private final GoalIndicatorService goalIndicatorService;

    @GetMapping("/{indicatorId}/goals")
    @PermitAll
    public ResponseEntity<List<GoalIndicatorResponse>> getGoalsByIndicator(@PathVariable Long indicatorId) {
        List<GoalIndicatorResponse> responses = goalIndicatorService.findGoalsByIndicator(indicatorId);
        return ResponseEntity.ok(responses);
    }
} 