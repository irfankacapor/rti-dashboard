package io.dashboard.controller;

import io.dashboard.dto.*;
import io.dashboard.exception.BadRequestException;
import io.dashboard.repository.DimTimeRepository;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import io.dashboard.service.DashboardDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.dashboard.exception.GlobalExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.security.PermitAll;


import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/dashboard-data")
@RequiredArgsConstructor
@Slf4j
public class DashboardDataController {
    private final DashboardDataService dashboardDataService;
    private final DimTimeRepository dimTimeRepository;
    private final FactIndicatorValueRepository factRepository;
    private final IndicatorRepository indicatorRepository;

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @GetMapping("/{dashboardId}")
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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
        throw new UnsupportedOperationException("This endpoint has been moved to IndicatorController");
    }

    @GetMapping("/real-time-updates/{dashboardId}")
    @PermitAll
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
            @RequestParam(defaultValue = "12") int months,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) String dimension) {
        throw new UnsupportedOperationException("This endpoint has been moved to IndicatorController");
    }
    
    private int convertRangeToMonths(String range) {
        if (range == null || range.isEmpty()) {
            return 12; // default
        }
        
        // Parse range like "1Y", "6M", "3M", etc.
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

    @GetMapping("/export/{dashboardId}")
    @PermitAll
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
        throw new UnsupportedOperationException("This endpoint has been moved to IndicatorController");
    }

    @GetMapping("/quality-metrics/{dashboardId}")
    @PermitAll
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
    @PermitAll
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
    @PermitAll
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

    @PermitAll
    @GetMapping("/dashboard-with-relationships")
    public ResponseEntity<DashboardWithRelationshipsResponse> getDashboardWithRelationships() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Controller Authentication: {}", auth);
        if (auth != null) {
            logger.info("Controller Principal: {}", auth.getPrincipal());
            logger.info("Controller Authorities: {}", auth.getAuthorities());
            logger.info("Controller Authenticated: {}", auth.isAuthenticated());
        } else {
            logger.warn("Controller Authentication is null");
        }
        try {
            DashboardWithRelationshipsResponse response = dashboardDataService.getDashboardWithRelationships();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving dashboard with relationships: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/historical/{indicatorId}/sample-data")
    public ResponseEntity<HistoricalDataResponse> createSampleHistoricalData(@PathVariable Long indicatorId) {
        throw new UnsupportedOperationException("This endpoint has been moved to IndicatorController");
    }
} 