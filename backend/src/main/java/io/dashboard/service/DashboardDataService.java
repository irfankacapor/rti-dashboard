package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.model.*;
import io.dashboard.model.SubareaIndicator;
import io.dashboard.repository.*;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardDataService {
    private final DashboardRepository dashboardRepository;
    private final DashboardWidgetRepository dashboardWidgetRepository;
    private final PerformanceScoreRepository performanceScoreRepository;
    private final IndicatorRepository indicatorRepository;
    private final SubareaRepository subareaRepository;
    private final AreaRepository areaRepository;
    private final FactIndicatorValueRepository factIndicatorValueRepository;
    private final GoalService goalService;
    private final GoalGroupService goalGroupService;
    private final GoalIndicatorService goalIndicatorService;
    private final SubareaService subareaService;
    private final SubareaIndicatorRepository subareaIndicatorRepository;

    @Cacheable(value = "dashboardData", key = "#dashboardId")
    public DashboardDataResponse getDashboardData(Long dashboardId) {
        log.debug("Fetching dashboard data for ID: {}", dashboardId);
        
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
        
        List<DashboardWidget> widgets = dashboardWidgetRepository.findByDashboardId(dashboardId);
        
        List<WidgetDataResponse> widgetData = widgets.stream()
                .map(this::getWidgetData)
                .collect(Collectors.toList());
        
        DashboardDataResponse response = new DashboardDataResponse();
        response.setLastUpdated(LocalDateTime.now());
        // Note: The existing DashboardDataResponse expects areas and performanceOverview
        // We'll need to adapt this to work with the existing structure
        return response;
    }

    public WidgetDataResponse getWidgetData(DashboardWidget widget) {
        log.debug("Fetching widget data for widget ID: {}", widget.getId());
        
        WidgetDataResponse response = new WidgetDataResponse();
        response.setWidgetId(widget.getId());
        response.setWidgetType(widget.getWidgetType());
        response.setTitle(widget.getTitle());
        response.setPosition(widget.getPositionX() + "," + widget.getPositionY());
        response.setSize(widget.getWidth() + "x" + widget.getHeight());
        response.setConfig(widget.getConfig());
        
        switch (widget.getWidgetType()) {
            case AREA:
                response.setData(getAreaData(widget));
                break;
            case SUBAREA:
                response.setData(getSubareaData(widget));
                break;
            case INDICATOR:
                response.setData(getIndicatorData(widget));
                break;
            case GOAL:
                response.setData(getGoalData(widget));
                break;
            default:
                response.setData(Collections.emptyMap());
        }
        
        return response;
    }

    @Cacheable(value = "performanceMetrics", key = "#areaId")
    public PerformanceMetricsResponse getPerformanceMetrics(Long areaId) {
        log.debug("Fetching performance metrics for area ID: {}", areaId);
        
        List<Subarea> subareas = subareaRepository.findByAreaId(areaId);
        List<PerformanceMetric> metrics = new ArrayList<>();
        
        for (Subarea subarea : subareas) {
            List<PerformanceScore> scores = performanceScoreRepository.findBySubareaId(subarea.getId());
            if (!scores.isEmpty()) {
                PerformanceScore latestScore = scores.get(scores.size() - 1);
                PerformanceMetric metric = new PerformanceMetric();
                metric.setSubareaId(subarea.getId());
                metric.setSubareaName(subarea.getName());
                metric.setCurrentScore(latestScore.getScore());
                metric.setColorCode(latestScore.getColorCode());
                metric.setTrend(calculateTrend(scores));
                metric.setLastUpdated(latestScore.getCalculatedAt());
                metrics.add(metric);
            }
        }
        
        PerformanceMetricsResponse response = new PerformanceMetricsResponse();
        response.setAreaId(areaId);
        response.setMetrics(metrics);
        response.setAverageScore(metrics.stream().mapToDouble(PerformanceMetric::getCurrentScore).average().orElse(0.0));
        return response;
    }

    public DataAggregationResponse getDataAggregation(Long indicatorId, String aggregationType, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching data aggregation for indicator ID: {} with type: {}", indicatorId, aggregationType);
        
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorId(indicatorId);
        
        Map<String, Object> aggregatedData = new HashMap<>();
        
        switch (aggregationType.toUpperCase()) {
            case "SUM":
                aggregatedData.put("value", values.stream()
                        .mapToDouble(v -> v.getValue().doubleValue())
                        .sum());
                break;
            case "AVERAGE":
                aggregatedData.put("value", values.stream()
                        .mapToDouble(v -> v.getValue().doubleValue())
                        .average()
                        .orElse(0.0));
                break;
            case "MAX":
                aggregatedData.put("value", values.stream()
                        .mapToDouble(v -> v.getValue().doubleValue())
                        .max()
                        .orElse(0.0));
                break;
            case "MIN":
                aggregatedData.put("value", values.stream()
                        .mapToDouble(v -> v.getValue().doubleValue())
                        .min()
                        .orElse(0.0));
                break;
            default:
                aggregatedData.put("value", 0.0);
        }
        
        aggregatedData.put("count", values.size());
        aggregatedData.put("startDate", startDate);
        aggregatedData.put("endDate", endDate);
        
        DataAggregationResponse response = new DataAggregationResponse();
        response.setIndicatorId(indicatorId);
        response.setAggregationType(aggregationType);
        response.setData(aggregatedData);
        return response;
    }

    public RealTimeUpdateResponse getRealTimeUpdates(Long dashboardId) {
        log.debug("Fetching real-time updates for dashboard ID: {}", dashboardId);
        
        List<DashboardWidget> widgets = dashboardWidgetRepository.findByDashboardId(dashboardId);
        List<WidgetUpdate> updates = new ArrayList<>();
        
        for (DashboardWidget widget : widgets) {
            WidgetUpdate update = new WidgetUpdate();
            update.setWidgetId(widget.getId());
            update.setLastUpdated(LocalDateTime.now());
            update.setHasChanges(Math.random() > 0.5); // Simulate changes
            updates.add(update);
        }
        
        RealTimeUpdateResponse response = new RealTimeUpdateResponse();
        response.setDashboardId(dashboardId);
        response.setUpdates(updates);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public HistoricalDataResponse getHistoricalData(Long indicatorId, int months) {
        return getHistoricalData(indicatorId, months, null);
    }

    public HistoricalDataResponse getHistoricalData(Long indicatorId, int months, String dimension) {
        log.debug("Fetching historical data for indicator ID: {} for {} months, dimension: {}", indicatorId, months, dimension);
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);
        
        // Check if indicator exists first
        if (!indicatorRepository.existsById(indicatorId)) {
            log.warn("Indicator with ID {} not found", indicatorId);
            HistoricalDataResponse response = new HistoricalDataResponse();
            response.setIndicatorId(indicatorId);
            response.setDataPoints(new ArrayList<>());
            response.setStartDate(startDate);
            response.setEndDate(endDate);
            return response;
        }
        
        List<FactIndicatorValue> values;
        List<String> availableDimensions = new ArrayList<>();
        
        if (dimension != null && !dimension.isEmpty()) {
            // Fetch data for specific dimension
            switch (dimension.toLowerCase()) {
                case "time":
                    values = factIndicatorValueRepository.findByIndicatorIdWithTime(indicatorId);
                    availableDimensions.add("time");
                    break;
                case "location":
                    values = factIndicatorValueRepository.findByIndicatorIdWithEagerLoading(indicatorId);
                    availableDimensions.add("location");
                    break;
                default:
                    // For custom dimensions, fetch with generics
                    values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
                    availableDimensions.add(dimension);
                    break;
            }
        } else {
            // Fetch all available data to determine dimensions
            values = factIndicatorValueRepository.findByIndicatorIdWithGenerics(indicatorId);
            
            // Determine available dimensions
            if (values.stream().anyMatch(v -> v.getTime() != null)) {
                availableDimensions.add("time");
            }
            if (values.stream().anyMatch(v -> v.getLocation() != null)) {
                availableDimensions.add("location");
            }
            
            // Add custom dimensions from generics
            Set<String> customDims = values.stream()
                .filter(v -> v.getGenerics() != null)
                .flatMap(v -> v.getGenerics().stream())
                .filter(g -> g.getDimensionName() != null)
                .map(DimGeneric::getDimensionName)
                .collect(Collectors.toSet());
            availableDimensions.addAll(customDims);
        }
        
        List<HistoricalDataPoint> dataPoints = values.stream()
                .map(v -> {
                    HistoricalDataPoint point = new HistoricalDataPoint();
                    
                    // Build dimensional context map
                    Map<String, String> dimensions = new HashMap<>();
                    if (v.getTime() != null) {
                        dimensions.put("time", v.getTime().getValue());
                    }
                    if (v.getLocation() != null) {
                        dimensions.put("location", v.getLocation().getName());
                    }
                    if (v.getGenerics() != null) {
                        for (DimGeneric generic : v.getGenerics()) {
                            if (generic.getDimensionName() != null) {
                                dimensions.put(generic.getDimensionName(), generic.getValue());
                            }
                        }
                    }
                    
                    if (dimension != null && !dimension.isEmpty()) {
                        // Map data based on requested dimension
                        switch (dimension.toLowerCase()) {
                            case "time":
                                point.setTimestamp(v.getTime() != null ? v.getTime().getValue() : "Unknown");
                                break;
                            case "location":
                                point.setTimestamp(v.getLocation() != null ? v.getLocation().getName() : "Unknown");
                                break;
                            default:
                                // For custom dimensions, find the matching generic
                                String dimensionValue = v.getGenerics() != null ? 
                                    v.getGenerics().stream()
                                        .filter(g -> dimension.equals(g.getDimensionName()))
                                        .map(DimGeneric::getValue)
                                        .findFirst()
                                        .orElse("Unknown") : "Unknown";
                                point.setTimestamp(dimensionValue);
                                break;
                        }
                    } else {
                        // Default to time if no dimension specified
                        point.setTimestamp(v.getTime() != null ? v.getTime().getValue() : "Unknown");
                    }
                    
                    point.setValue(v.getValue().doubleValue());
                    point.setDimensions(dimensions);
                    return point;
                })
                .collect(Collectors.toList());
        
        HistoricalDataResponse response = new HistoricalDataResponse();
        response.setIndicatorId(indicatorId);
        response.setDataPoints(dataPoints);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setDimensions(availableDimensions);
        
        return response;
    }

    public DataExportResponse getDataExport(Long dashboardId, String format) {
        log.debug("Exporting dashboard data for ID: {} in format: {}", dashboardId, format);
        
        DashboardDataResponse dashboardData = getDashboardData(dashboardId);
        
        String exportData = switch (format.toUpperCase()) {
            case "JSON" -> convertToJson(dashboardData);
            case "CSV" -> convertToCsv(dashboardData);
            case "XML" -> convertToXml(dashboardData);
            default -> convertToJson(dashboardData);
        };
        
        DataExportResponse response = new DataExportResponse();
        response.setDashboardId(dashboardId);
        response.setFormat(format);
        response.setData(exportData);
        response.setExportedAt(LocalDateTime.now());
        return response;
    }

    public DataValidationResponse getDataValidation(Long indicatorId) {
        log.debug("Validating data for indicator ID: {}", indicatorId);
        
        // Check if indicator exists
        indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new RuntimeException("Indicator not found"));
        
        List<FactIndicatorValue> values = factIndicatorValueRepository.findByIndicatorId(indicatorId);
        
        long totalRecords = values.size();
        long validRecords = values.stream()
                .filter(v -> v.getValue() != null && v.getValue().doubleValue() >= 0)
                .count();
        long invalidRecords = totalRecords - validRecords;
        
        List<String> validationErrors = new ArrayList<>();
        if (invalidRecords > 0) {
            validationErrors.add("Found " + invalidRecords + " invalid records");
        }
        
        DataValidationResponse response = new DataValidationResponse();
        response.setIndicatorId(indicatorId);
        response.setTotalRecords(totalRecords);
        response.setValidRecords(validRecords);
        response.setInvalidRecords(invalidRecords);
        response.setValidationErrors(validationErrors);
        response.setIsValid(invalidRecords == 0);
        return response;
    }

    public DataQualityMetricsResponse getDataQualityMetrics(Long dashboardId) {
        log.debug("Fetching data quality metrics for dashboard ID: {}", dashboardId);
        
        Dashboard dashboard = dashboardRepository.findById(dashboardId)
                .orElseThrow(() -> new RuntimeException("Dashboard not found"));
        
        List<DashboardWidget> widgets = dashboardWidgetRepository.findByDashboardId(dashboardId);
        
        double completeness = calculateCompleteness(widgets);
        double accuracy = calculateAccuracy(widgets);
        double timeliness = calculateTimeliness(widgets);
        
        DataQualityMetricsResponse response = new DataQualityMetricsResponse();
        response.setDashboardId(dashboardId);
        response.setCompleteness(completeness);
        response.setAccuracy(accuracy);
        response.setTimeliness(timeliness);
        response.setOverallScore((completeness + accuracy + timeliness) / 3.0);
        response.setLastCalculated(LocalDateTime.now());
        return response;
    }

    public DataRefreshStatusResponse getDataRefreshStatus(Long dashboardId) {
        log.debug("Fetching data refresh status for dashboard ID: {}", dashboardId);
        
        // Check if dashboard exists, but don't throw exception if not found
        Optional<Dashboard> dashboard = dashboardRepository.findById(dashboardId);
        
        DataRefreshStatusResponse response = new DataRefreshStatusResponse();
        response.setDashboardId(dashboardId);
        response.setLastRefresh(LocalDateTime.now());
        response.setNextRefresh(LocalDateTime.now().plusHours(1));
        response.setRefreshInterval("1 hour");
        response.setIsAutoRefresh(true);
        response.setStatus(dashboard.isPresent() ? "ACTIVE" : "NOT_FOUND");
        return response;
    }

    // Private helper methods
    private Map<String, Object> getAreaData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "area");
        data.put("data", "Area performance data");
        return data;
    }

    private Map<String, Object> getSubareaData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "subarea");
        data.put("data", "Subarea performance data");
        return data;
    }

    private Map<String, Object> getIndicatorData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "indicator");
        data.put("data", "Indicator data");
        return data;
    }

    private Map<String, Object> getGoalData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "goal");
        data.put("data", "Goal data");
        return data;
    }

    private Map<String, Object> getPerformanceChartData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", "line");
        data.put("labels", Arrays.asList("Jan", "Feb", "Mar", "Apr", "May"));
        data.put("datasets", Arrays.asList(
                Map.of("label", "Performance", "data", Arrays.asList(65, 70, 75, 80, 85))
        ));
        return data;
    }

    private Map<String, Object> getIndicatorTableData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("columns", Arrays.asList("Indicator", "Value", "Target", "Status"));
        data.put("rows", Arrays.asList(
                Arrays.asList("GDP Growth", "3.2%", "3.5%", "Below Target"),
                Arrays.asList("Unemployment", "4.1%", "4.0%", "Above Target")
        ));
        return data;
    }

    private Map<String, Object> getMetricCardData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", "85.2");
        data.put("unit", "%");
        data.put("trend", "+2.1");
        data.put("color", "green");
        return data;
    }

    private Map<String, Object> getHeatmapData(DashboardWidget widget) {
        Map<String, Object> data = new HashMap<>();
        data.put("xAxis", Arrays.asList("Q1", "Q2", "Q3", "Q4"));
        data.put("yAxis", Arrays.asList("Area 1", "Area 2", "Area 3"));
        data.put("values", Arrays.asList(
                Arrays.asList(80, 85, 90, 88),
                Arrays.asList(75, 80, 85, 82),
                Arrays.asList(70, 75, 80, 78)
        ));
        return data;
    }

    private String calculateTrend(List<PerformanceScore> scores) {
        if (scores.size() < 2) return "STABLE";
        
        double current = scores.get(scores.size() - 1).getScore();
        double previous = scores.get(scores.size() - 2).getScore();
        
        if (current > previous + 2) return "IMPROVING";
        if (current < previous - 2) return "DECLINING";
        return "STABLE";
    }

    private String convertToJson(DashboardDataResponse data) {
        // Simplified JSON conversion
        return "{\"dashboardId\":" + data.getLastUpdated() + "}";
    }

    private String convertToCsv(DashboardDataResponse data) {
        // Simplified CSV conversion
        return "Dashboard ID,Last Updated\n" + data.getLastUpdated();
    }

    private String convertToXml(DashboardDataResponse data) {
        // Simplified XML conversion
        return "<dashboard><lastUpdated>" + data.getLastUpdated() + "</lastUpdated></dashboard>";
    }

    private double calculateCompleteness(List<DashboardWidget> widgets) {
        return widgets.stream()
                .mapToDouble(w -> Math.random() * 20 + 80) // Simulate 80-100% completeness
                .average()
                .orElse(0.0);
    }

    private double calculateAccuracy(List<DashboardWidget> widgets) {
        return widgets.stream()
                .mapToDouble(w -> Math.random() * 15 + 85) // Simulate 85-100% accuracy
                .average()
                .orElse(0.0);
    }

    private double calculateTimeliness(List<DashboardWidget> widgets) {
        return widgets.stream()
                .mapToDouble(w -> Math.random() * 10 + 90) // Simulate 90-100% timeliness
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public DashboardWithRelationshipsResponse getDashboardWithRelationships() {
        log.debug("Fetching dashboard data with goal-subarea relationships");
        
        try {
            // Fetch all required data
            List<AreaResponse> areas = areaRepository.findAll().stream()
                    .map(this::mapAreaToResponse)
                    .collect(Collectors.toList());
            
            List<SubareaResponse> subareas = subareaService.findAll();
            
            List<GoalResponse> goals = goalService.findAll();
            
            List<GoalGroupResponse> goalGroups = goalGroupService.findAll();
            
            log.debug("Found {} areas, {} subareas, {} goals, {} goal groups", 
                     areas.size(), subareas.size(), goals.size(), goalGroups.size());
            
            // Build relationship mappings
            Map<String, List<String>> goalToSubareas = new HashMap<>();
            Map<String, List<String>> subareaToGoals = new HashMap<>();
            
            // For each goal, find connected subareas through indicators
            for (GoalResponse goal : goals) {
                List<String> connectedSubareaIds = new ArrayList<>();
                
                try {
                    // Get indicators for this goal
                    List<GoalIndicatorResponse> goalIndicators = goalIndicatorService.findIndicatorsByGoal(goal.getId());
                    log.debug("Goal {} has {} indicators", goal.getId(), goalIndicators.size());
                    
                    // For each indicator, find connected subareas
                    for (GoalIndicatorResponse goalIndicator : goalIndicators) {
                        List<SubareaIndicator> subareaIndicators = subareaIndicatorRepository.findByIndicatorId(goalIndicator.getIndicatorId());
                        log.debug("Indicator {} has {} subarea relationships", goalIndicator.getIndicatorId(), subareaIndicators.size());
                        
                        for (SubareaIndicator subareaIndicator : subareaIndicators) {
                            String subareaId = subareaIndicator.getSubarea().getId().toString();
                            connectedSubareaIds.add(subareaId);
                            
                            // Build reverse mapping
                            subareaToGoals.computeIfAbsent(subareaId, k -> new ArrayList<>()).add(goal.getId().toString());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch relationships for goal {}: {}", goal.getId(), e.getMessage());
                }
                
                goalToSubareas.put(goal.getId().toString(), connectedSubareaIds);
                log.debug("Goal {} connected to {} subareas: {}", goal.getId(), connectedSubareaIds.size(), connectedSubareaIds);
            }
            
            log.debug("Final mappings - goalToSubareas: {} entries, subareaToGoals: {} entries", 
                     goalToSubareas.size(), subareaToGoals.size());
            
            // Create response
            DashboardWithRelationshipsResponse response = new DashboardWithRelationshipsResponse();
            response.setAreas(areas);
            response.setSubareas(subareas);
            response.setGoals(goals);
            response.setGoalGroups(goalGroups);
            response.setLastUpdated(LocalDateTime.now());
            
            DashboardWithRelationshipsResponse.RelationshipMappings relationships = new DashboardWithRelationshipsResponse.RelationshipMappings();
            relationships.setGoalToSubareas(goalToSubareas);
            relationships.setSubareaToGoals(subareaToGoals);
            response.setRelationships(relationships);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error fetching dashboard with relationships: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch dashboard data with relationships", e);
        }
    }
    
    private AreaResponse mapAreaToResponse(Area area) {
        AreaResponse response = new AreaResponse();
        response.setId(area.getId());
        response.setName(area.getName());
        response.setDescription(area.getDescription());
        response.setCode(area.getCode());
        response.setCreatedAt(area.getCreatedAt());
        return response;
    }
} 