package io.dashboard.controller;

import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.IndicatorValueUpdate;
import io.dashboard.dto.IndicatorValueCreate;
import io.dashboard.dto.IndicatorChartResponse;
import io.dashboard.dto.IndicatorDimensionsResponse;
import io.dashboard.dto.IndicatorSubareaDirectionResponse;
import io.dashboard.dto.IndicatorDirectionUpdateRequest;
import io.dashboard.dto.HistoricalDataResponse;
import io.dashboard.dto.DataValidationResponse;
import io.dashboard.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.annotation.Secured;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IndicatorController {
    private final IndicatorService indicatorService;

    @GetMapping("/indicators")
    @PermitAll
    public List<IndicatorResponse> getAllIndicators() {
        return indicatorService.findAll();
    }

    @GetMapping("/indicators/{id}")
    @PermitAll
    public IndicatorResponse getIndicatorById(@PathVariable Long id) {
        return indicatorService.findById(id);
    }

    @GetMapping("/subareas/{subareaId}/indicators")
    @PermitAll
    public List<IndicatorResponse> getIndicatorsBySubarea(@PathVariable Long subareaId) {
        return indicatorService.findByFactSubareaId(subareaId);
    }

    @PostMapping("/indicators")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<IndicatorResponse> createIndicator(@Valid @RequestBody IndicatorCreateRequest request) {
        IndicatorResponse response = indicatorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/indicators/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public IndicatorResponse updateIndicator(@PathVariable Long id, @Valid @RequestBody IndicatorUpdateRequest request) {
        return indicatorService.update(id, request);
    }

    @DeleteMapping("/indicators/{id}")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteIndicator(@PathVariable Long id) {
        indicatorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/indicators/{id}/with-data")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> deleteIndicatorWithData(@PathVariable Long id) {
        indicatorService.deleteWithData(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/indicators/{id}/values")
    @PermitAll
    public ResponseEntity<IndicatorValuesResponse> getIndicatorValues(@PathVariable Long id) {
        IndicatorValuesResponse response = indicatorService.getIndicatorValues(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/indicators/{id}/values")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> createIndicatorValues(@PathVariable Long id, @RequestBody List<IndicatorValueCreate> newValues) {
        indicatorService.createIndicatorValues(id, newValues);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/indicators/{id}/values")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> updateIndicatorValues(@PathVariable Long id, @RequestBody List<IndicatorValueUpdate> updates) {
        indicatorService.updateIndicatorValues(id, updates);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/indicator-types")
    @PermitAll
    public ResponseEntity<List<String>> getIndicatorTypes() {
        List<String> types = java.util.Arrays.stream(io.dashboard.model.Direction.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/indicators/{id}/chart")
    @PermitAll
    public ResponseEntity<IndicatorChartResponse> getIndicatorChart(@PathVariable Long id) {
        IndicatorChartResponse response = indicatorService.getIndicatorChart(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/dimensions")
    @PermitAll
    public ResponseEntity<IndicatorDimensionsResponse> getIndicatorDimensions(@PathVariable Long id) {
        IndicatorDimensionsResponse response = indicatorService.getIndicatorDimensions(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/indicators/{id}/subarea-directions")
    @PermitAll
    public ResponseEntity<List<IndicatorSubareaDirectionResponse>> getIndicatorSubareaDirections(@PathVariable Long id) {
        List<IndicatorSubareaDirectionResponse> response = indicatorService.getIndicatorSubareaDirections(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/indicators/{indicatorId}/subareas/{subareaId}/direction")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<Void> updateIndicatorDirectionForSubarea(
            @PathVariable Long indicatorId,
            @PathVariable Long subareaId,
            @RequestBody @Valid IndicatorDirectionUpdateRequest request) {
        indicatorService.updateIndicatorDirectionForSubarea(indicatorId, subareaId, request.getDirection());
        return ResponseEntity.noContent().build();
    }
    


    @GetMapping("/indicators/{id}/historical")
    @PermitAll
    public ResponseEntity<HistoricalDataResponse> getHistoricalData(
            @PathVariable Long id,
            @RequestParam(defaultValue = "12") int months,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String dimension) {
        HistoricalDataResponse response = indicatorService.getHistoricalData(id, months, range, dimension);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/validation")
    @PermitAll
    public ResponseEntity<DataValidationResponse> validateIndicatorData(@PathVariable Long id) {
        DataValidationResponse response = indicatorService.validateIndicatorData(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/aggregation")
    @PermitAll
    public ResponseEntity<Map<String, Double>> getAggregatedByDimension(
            @PathVariable Long id,
            @RequestParam String dimension) {
        Map<String, Double> response = indicatorService.getAggregatedByDimension(id, dimension);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/indicators/{id}/sample-data")
    @Secured({"ROLE_ADMIN", "ROLE_MANAGER"})
    public ResponseEntity<HistoricalDataResponse> createSampleHistoricalData(@PathVariable Long id) {
        HistoricalDataResponse response = indicatorService.createSampleHistoricalData(id);
        return ResponseEntity.ok(response);
    }
} 