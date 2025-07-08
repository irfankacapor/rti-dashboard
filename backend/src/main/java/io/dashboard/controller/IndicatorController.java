package io.dashboard.controller;

import io.dashboard.dto.IndicatorCreateRequest;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.dto.IndicatorUpdateRequest;
import io.dashboard.dto.SubareaIndicatorRequest;
import io.dashboard.dto.IndicatorValuesResponse;
import io.dashboard.dto.IndicatorValueUpdate;
import io.dashboard.dto.IndicatorChartResponse;
import io.dashboard.dto.IndicatorDimensionsResponse;
import io.dashboard.dto.HistoricalDataResponse;
import io.dashboard.dto.DataValidationResponse;
import io.dashboard.service.IndicatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/indicator-types")
    public ResponseEntity<List<String>> getIndicatorTypes() {
        List<String> types = java.util.Arrays.stream(io.dashboard.model.Direction.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/indicators/{id}/aggregate")
    public ResponseEntity<Map<String, Double>> getIndicatorAggregatedByDimension(
            @PathVariable Long id,
            @RequestParam String dimension) {
        Map<String, Double> result = indicatorService.getAggregatedByDimension(id, dimension);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/indicators/{id}/raw-data")
    public ResponseEntity<IndicatorValuesResponse> getIndicatorRawData(@PathVariable Long id) {
        IndicatorValuesResponse response = indicatorService.getIndicatorValues(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/chart")
    public ResponseEntity<IndicatorChartResponse> getIndicatorChart(
            @PathVariable Long id,
            @RequestParam String aggregateBy,
            @RequestParam(required = false) Long subareaId) {
        IndicatorChartResponse response = indicatorService.getIndicatorChart(id, aggregateBy, subareaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/dimensions")
    public ResponseEntity<IndicatorDimensionsResponse> getIndicatorDimensions(@PathVariable Long id) {
        IndicatorDimensionsResponse response = indicatorService.getIndicatorDimensions(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{id}/historical")
    public ResponseEntity<HistoricalDataResponse> getHistoricalData(
            @PathVariable Long id,
            @RequestParam(defaultValue = "12") int months,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String dimension) {
        // If range is provided, convert to months
        int monthsToFetch = months;
        if (range != null && !range.isEmpty()) {
            monthsToFetch = convertRangeToMonths(range);
        }
        HistoricalDataResponse response = indicatorService.getHistoricalData(id, monthsToFetch, dimension);
        return ResponseEntity.ok(response);
    }

    private int convertRangeToMonths(String range) {
        if (range == null || range.isEmpty()) {
            return 12; // default
        }
        String unit = range.substring(range.length() - 1).toUpperCase();
        int value = Integer.parseInt(range.substring(0, range.length() - 1));
        switch (unit) {
            case "Y":
                return value * 12;
            case "M":
                return value;
            case "W":
                return (int) Math.ceil(value / 4.0); // approximate weeks to months
            case "D":
                return (int) Math.ceil(value / 30.0); // approximate days to months
            default:
                return 12; // default fallback
        }
    }

    @GetMapping("/indicators/{id}/validation")
    public ResponseEntity<DataValidationResponse> validateIndicatorData(@PathVariable Long id) {
        DataValidationResponse response = indicatorService.getDataValidation(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/indicators/{id}/sample-data")
    public ResponseEntity<HistoricalDataResponse> createSampleHistoricalData(@PathVariable Long id) {
        HistoricalDataResponse response = indicatorService.createSampleHistoricalData(id);
        return ResponseEntity.ok(response);
    }
} 