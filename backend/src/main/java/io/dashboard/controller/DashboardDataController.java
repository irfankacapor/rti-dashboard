package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.exception.BadRequestException;
import io.dashboard.service.DashboardDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard-data")
@RequiredArgsConstructor
@Slf4j
public class DashboardDataController {
    private final DashboardDataService dashboardDataService;

    @GetMapping("/{dashboardId}")
    public ResponseEntity<DashboardDataResponse> getDashboardData(@PathVariable Long dashboardId) {
        log.info("Retrieving dashboard data for dashboard ID: {}", dashboardId);
        
        // Validate dashboard ID
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        
        try {
            DashboardDataResponse response = dashboardDataService.getDashboardData(dashboardId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error retrieving dashboard data for ID {}: {}", dashboardId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{dashboardId}/widgets/{widgetId}")
    public ResponseEntity<WidgetDataResponse> getWidgetData(
            @PathVariable Long dashboardId,
            @PathVariable Long widgetId) {
        log.info("Retrieving widget data for dashboard ID: {} and widget ID: {}", dashboardId, widgetId);
        
        // Validate IDs
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        if (widgetId == null || widgetId <= 0) {
            throw new BadRequestException("Widget ID must be a positive number");
        }
        
        try {
            // This would need to be implemented in the service to get widget by ID
            // For now, we'll return a placeholder response
            WidgetDataResponse response = new WidgetDataResponse();
            response.setWidgetId(widgetId);
            response.setTitle("Widget " + widgetId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving widget data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/performance-metrics/{areaId}")
    public ResponseEntity<PerformanceMetricsResponse> getPerformanceMetrics(@PathVariable Long areaId) {
        log.info("Retrieving performance metrics for area ID: {}", areaId);
        
        // Validate area ID
        if (areaId == null || areaId <= 0) {
            throw new BadRequestException("Area ID must be a positive number");
        }
        
        try {
            PerformanceMetricsResponse response = dashboardDataService.getPerformanceMetrics(areaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving performance metrics for area {}: {}", areaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/aggregation")
    public ResponseEntity<DataAggregationResponse> getDataAggregation(
            @RequestParam Long indicatorId,
            @RequestParam String aggregationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Retrieving data aggregation for indicator ID: {} with type: {}", indicatorId, aggregationType);
        
        // Validate parameters
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Indicator ID must be a positive number");
        }
        if (aggregationType == null || aggregationType.trim().isEmpty()) {
            throw new BadRequestException("Aggregation type is required");
        }
        
        try {
            LocalDateTime start = startDate != null ? startDate : LocalDateTime.now().minusMonths(1);
            LocalDateTime end = endDate != null ? endDate : LocalDateTime.now();
            DataAggregationResponse response = dashboardDataService.getDataAggregation(indicatorId, aggregationType, start, end);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving data aggregation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/real-time-updates/{dashboardId}")
    public ResponseEntity<RealTimeUpdateResponse> getRealTimeUpdates(@PathVariable Long dashboardId) {
        log.info("Retrieving real-time updates for dashboard ID: {}", dashboardId);
        
        // Validate dashboard ID
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        
        try {
            RealTimeUpdateResponse response = dashboardDataService.getRealTimeUpdates(dashboardId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving real-time updates: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/historical/{indicatorId}")
    public ResponseEntity<HistoricalDataResponse> getHistoricalData(
            @PathVariable Long indicatorId,
            @RequestParam(defaultValue = "12") int months) {
        log.info("Retrieving historical data for indicator ID: {} for {} months", indicatorId, months);
        
        // Validate parameters
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Indicator ID must be a positive number");
        }
        if (months <= 0) {
            throw new BadRequestException("Months must be a positive number");
        }
        
        try {
            HistoricalDataResponse response = dashboardDataService.getHistoricalData(indicatorId, months);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving historical data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/export/{dashboardId}")
    public ResponseEntity<DataExportResponse> exportDashboardData(
            @PathVariable Long dashboardId,
            @RequestParam(defaultValue = "JSON") String format) {
        log.info("Exporting dashboard data for dashboard ID: {} in format: {}", dashboardId, format);
        
        // Validate dashboard ID
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        
        try {
            DataExportResponse response = dashboardDataService.getDataExport(dashboardId, format);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error exporting dashboard data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validation/{indicatorId}")
    public ResponseEntity<DataValidationResponse> validateIndicatorData(@PathVariable Long indicatorId) {
        log.info("Validating data for indicator ID: {}", indicatorId);
        
        // Validate indicator ID
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Indicator ID must be a positive number");
        }
        
        try {
            DataValidationResponse response = dashboardDataService.getDataValidation(indicatorId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating indicator data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/quality-metrics/{dashboardId}")
    public ResponseEntity<DataQualityMetricsResponse> getDataQualityMetrics(@PathVariable Long dashboardId) {
        log.info("Retrieving data quality metrics for dashboard ID: {}", dashboardId);
        
        // Validate dashboard ID
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        
        try {
            DataQualityMetricsResponse response = dashboardDataService.getDataQualityMetrics(dashboardId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving data quality metrics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/refresh-status/{dashboardId}")
    public ResponseEntity<DataRefreshStatusResponse> getDataRefreshStatus(@PathVariable Long dashboardId) {
        log.info("Retrieving data refresh status for dashboard ID: {}", dashboardId);
        
        // Validate dashboard ID
        if (dashboardId == null || dashboardId <= 0) {
            throw new BadRequestException("Dashboard ID must be a positive number");
        }
        
        try {
            DataRefreshStatusResponse response = dashboardDataService.getDataRefreshStatus(dashboardId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving data refresh status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/performance-metrics")
    public ResponseEntity<List<PerformanceMetricsResponse>> getAllPerformanceMetrics() {
        log.info("Retrieving all performance metrics");
        try {
            // This would need to be implemented in the service
            // For now, return empty list
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error retrieving all performance metrics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 