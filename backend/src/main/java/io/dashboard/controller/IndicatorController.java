package io.dashboard.controller;

import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.IndicatorValueUpdate;
import io.dashboard.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IndicatorController {
    private final IndicatorService indicatorService;

    @GetMapping("/indicators")
    public List<IndicatorResponse> getAllIndicators() {
        return indicatorService.findAll();
    }

    @GetMapping("/indicators/{id}")
    public IndicatorResponse getIndicatorById(@PathVariable Long id) {
        return indicatorService.findById(id);
    }

    @GetMapping("/subareas/{subareaId}/indicators")
    public List<IndicatorResponse> getIndicatorsBySubarea(@PathVariable Long subareaId) {
        return indicatorService.findBySubareaId(subareaId);
    }

    @PostMapping("/indicators")
    public ResponseEntity<IndicatorResponse> createIndicator(@Valid @RequestBody IndicatorCreateRequest request) {
        IndicatorResponse response = indicatorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/indicators/{id}")
    public IndicatorResponse updateIndicator(@PathVariable Long id, @Valid @RequestBody IndicatorUpdateRequest request) {
        return indicatorService.update(id, request);
    }

    @DeleteMapping("/indicators/{id}")
    public ResponseEntity<Void> deleteIndicator(@PathVariable Long id) {
        indicatorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/indicators/{id}/with-data")
    public ResponseEntity<Void> deleteIndicatorWithData(@PathVariable Long id) {
        indicatorService.deleteWithData(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/indicators/{indicatorId}/subareas/{subareaId}")
    public ResponseEntity<Void> assignIndicatorToSubarea(
            @PathVariable Long indicatorId,
            @PathVariable Long subareaId,
            @Valid @RequestBody SubareaIndicatorRequest request) {
        indicatorService.assignToSubarea(indicatorId, subareaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/indicators/{indicatorId}/subareas/{subareaId}")
    public ResponseEntity<Void> removeIndicatorFromSubarea(
            @PathVariable Long indicatorId,
            @PathVariable Long subareaId) {
        indicatorService.removeFromSubarea(indicatorId, subareaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/indicators/{id}/values")
    public ResponseEntity<IndicatorValuesResponse> getIndicatorValues(@PathVariable Long id) {
        IndicatorValuesResponse response = indicatorService.getIndicatorValues(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/indicators/{id}/values")
    public ResponseEntity<Void> updateIndicatorValues(@PathVariable Long id, @RequestBody List<IndicatorValueUpdate> updates) {
        indicatorService.updateIndicatorValues(id, updates);
        return ResponseEntity.noContent().build();
    }
} 