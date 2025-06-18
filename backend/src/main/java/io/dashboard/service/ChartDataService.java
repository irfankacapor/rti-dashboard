package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.entity.VisualizationType;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChartDataService {

    @Autowired
    private FactIndicatorValueRepository factIndicatorValueRepository;

    @Autowired
    private IndicatorRepository indicatorRepository;

    @Autowired
    private ChartDataFormattingService formattingService;

    public TimeSeriesDataResponse getTimeSeriesData(Long indicatorId, LocalDateTime startDate, LocalDateTime endDate) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }

        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator not found with ID: " + indicatorId);
        }

        List<FactIndicatorValue> data = factIndicatorValueRepository.findByIndicatorIdAndCreatedAtBetween(
                indicatorId, startDate, endDate);

        return formattingService.formatForLineChart(data);
    }

    public LocationComparisonResponse getLocationComparisonData(Long indicatorId, List<Long> locationIds) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }
        if (locationIds == null || locationIds.isEmpty()) {
            throw new BadRequestException("Location IDs are required");
        }

        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator not found with ID: " + indicatorId);
        }

        List<FactIndicatorValue> data = factIndicatorValueRepository.findByIndicatorId(indicatorId);
        // Filter by location IDs
        data = data.stream()
                .filter(fact -> fact.getLocation() != null && locationIds.contains(fact.getLocation().getId()))
                .collect(Collectors.toList());

        return formattingService.formatForBarChart(data, "location");
    }

    public DimensionBreakdownResponse getDimensionBreakdownData(Long indicatorId, String dimensionType) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }
        if (dimensionType == null || dimensionType.trim().isEmpty()) {
            throw new BadRequestException("Dimension type is required");
        }

        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator not found with ID: " + indicatorId);
        }

        List<FactIndicatorValue> data = factIndicatorValueRepository.findByIndicatorId(indicatorId);

        return formattingService.formatForPieChart(data, dimensionType);
    }

    public CorrelationDataResponse getIndicatorCorrelationData(List<Long> indicatorIds) {
        if (indicatorIds == null || indicatorIds.size() < 2) {
            throw new BadRequestException("At least two indicator IDs are required for correlation analysis");
        }
        if (indicatorIds.size() > 2) {
            throw new BadRequestException("Correlation analysis supports only two indicators");
        }
        if (Objects.equals(indicatorIds.get(0), indicatorIds.get(1))) {
            throw new BadRequestException("Indicator IDs for correlation must be different");
        }

        // Check if indicators exist
        for (Long id : indicatorIds) {
            if (!indicatorRepository.existsById(id)) {
                throw new ResourceNotFoundException("Indicator not found with ID: " + id);
            }
        }

        List<FactIndicatorValue> xData = factIndicatorValueRepository.findByIndicatorId(indicatorIds.get(0));
        List<FactIndicatorValue> yData = factIndicatorValueRepository.findByIndicatorId(indicatorIds.get(1));

        return formattingService.formatForScatterPlot(xData, yData);
    }

    public TrendAnalysisResponse getTrendAnalysisData(Long indicatorId, int periods) {
        if (indicatorId == null || indicatorId <= 0) {
            throw new BadRequestException("Invalid indicator ID");
        }
        if (periods <= 0) {
            throw new BadRequestException("Number of periods must be positive");
        }

        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator not found with ID: " + indicatorId);
        }

        List<FactIndicatorValue> data = factIndicatorValueRepository.findByIndicatorId(indicatorId);
        
        if (data.size() < periods) {
            throw new BadRequestException("Insufficient data for trend analysis. Need at least " + periods + " data points");
        }

        return formattingService.calculateTrendAnalysis(data, periods);
    }

    public TimeSeriesDataResponse getGoalProgressChartData(Long goalId) {
        if (goalId == null || goalId <= 0) {
            throw new BadRequestException("Invalid goal ID");
        }

        // This would typically involve goal-indicator relationships
        // For now, we'll return empty data structure
        TimeSeriesDataResponse response = new TimeSeriesDataResponse();
        response.setLabels(new ArrayList<>());
        response.setDatasets(new ArrayList<>());
        
        ChartConfig config = new ChartConfig();
        config.setTitle("Goal Progress");
        config.setXAxisLabel("Time");
        config.setYAxisLabel("Progress (%)");
        response.setChartConfig(config);

        return response;
    }

    public HeatmapDataResponse getSubareaPerformanceHeatmap(Long areaId) {
        if (areaId == null || areaId <= 0) {
            throw new BadRequestException("Invalid area ID");
        }

        // This would typically involve area-subarea relationships
        // For now, we'll return empty data structure
        HeatmapDataResponse response = new HeatmapDataResponse();
        response.setXLabels(new ArrayList<>());
        response.setYLabels(new ArrayList<>());
        response.setData(new ArrayList<>());
        
        HeatmapDataResponse.ColorScale colorScale = new HeatmapDataResponse.ColorScale();
        colorScale.setMinColor("#ff0000");
        colorScale.setMaxColor("#00ff00");
        colorScale.setMinValue(0.0);
        colorScale.setMaxValue(100.0);
        response.setColorScale(colorScale);

        ChartConfig config = new ChartConfig();
        config.setTitle("Subarea Performance Heatmap");
        response.setChartConfig(config);

        return response;
    }

    public Object formatChartDataForType(List<FactIndicatorValue> data, VisualizationType type) {
        if (data == null) {
            throw new BadRequestException("Data cannot be null");
        }
        if (type == null) {
            throw new BadRequestException("Visualization type cannot be null");
        }

        switch (type) {
            case LINE:
                return formattingService.formatForLineChart(data);
            case BAR:
                return formattingService.formatForBarChart(data, "category");
            case PIE:
                return formattingService.formatForPieChart(data, "category");
            case SCATTER:
                return formattingService.formatForScatterPlot(data, data);
            case AREA:
                return formattingService.formatForAreaChart(data);
            case HEATMAP:
                return formattingService.formatForHeatmap(data, "xDimension", "yDimension");
            case GAUGE:
                return formattingService.formatForGauge(data.isEmpty() ? null : data.get(0), null);
            default:
                throw new BadRequestException("Unsupported visualization type: " + type);
        }
    }
} 