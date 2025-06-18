package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.service.ChartDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/charts")
public class ChartDataController {

    @Autowired
    private ChartDataService chartDataService;

    @GetMapping("/indicators/{indicatorId}/time-series")
    public ResponseEntity<TimeSeriesDataResponse> getTimeSeriesData(
            @PathVariable Long indicatorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        TimeSeriesDataResponse response = chartDataService.getTimeSeriesData(indicatorId, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{indicatorId}/location-comparison")
    public ResponseEntity<LocationComparisonResponse> getLocationComparison(
            @PathVariable Long indicatorId,
            @RequestParam List<Long> locationIds) {
        
        LocationComparisonResponse response = chartDataService.getLocationComparisonData(indicatorId, locationIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{indicatorId}/dimension-breakdown")
    public ResponseEntity<DimensionBreakdownResponse> getDimensionBreakdown(
            @PathVariable Long indicatorId,
            @RequestParam String dimensionType) {
        
        DimensionBreakdownResponse response = chartDataService.getDimensionBreakdownData(indicatorId, dimensionType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/correlation")
    public ResponseEntity<CorrelationDataResponse> getCorrelationData(
            @RequestParam List<Long> indicatorIds) {
        
        CorrelationDataResponse response = chartDataService.getIndicatorCorrelationData(indicatorIds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/goals/{goalId}/progress")
    public ResponseEntity<TimeSeriesDataResponse> getGoalProgressChart(
            @PathVariable Long goalId) {
        
        TimeSeriesDataResponse response = chartDataService.getGoalProgressChartData(goalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/areas/{areaId}/heatmap")
    public ResponseEntity<HeatmapDataResponse> getAreaHeatmap(
            @PathVariable Long areaId) {
        
        HeatmapDataResponse response = chartDataService.getSubareaPerformanceHeatmap(areaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indicators/{indicatorId}/trend")
    public ResponseEntity<TrendAnalysisResponse> getTrendAnalysis(
            @PathVariable Long indicatorId,
            @RequestParam(defaultValue = "12") int periods) {
        
        TrendAnalysisResponse response = chartDataService.getTrendAnalysisData(indicatorId, periods);
        return ResponseEntity.ok(response);
    }
} 