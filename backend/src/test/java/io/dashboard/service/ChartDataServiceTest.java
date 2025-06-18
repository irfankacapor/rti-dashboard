package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.entity.VisualizationType;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.FactIndicatorValue;
import io.dashboard.model.Indicator;
import io.dashboard.repository.FactIndicatorValueRepository;
import io.dashboard.repository.IndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChartDataServiceTest {

    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private ChartDataFormattingService formattingService;

    @InjectMocks
    private ChartDataService chartDataService;

    private Indicator testIndicator;
    private FactIndicatorValue testFactValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        testIndicator = new Indicator();
        testIndicator.setId(1L);
        testIndicator.setName("Test Indicator");

        testFactValue = new FactIndicatorValue();
        testFactValue.setId(1L);
        testFactValue.setIndicator(testIndicator);
        testFactValue.setValue(BigDecimal.valueOf(100.0));

        startDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2023, 12, 31, 23, 59);
    }

    @Test
    void getTimeSeriesData_withValidData_shouldReturnFormattedData() {
        // Given
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorIdAndCreatedAtBetween(1L, startDate, endDate))
                .thenReturn(Arrays.asList(testFactValue));
        
        TimeSeriesDataResponse expectedResponse = new TimeSeriesDataResponse();
        when(formattingService.formatForLineChart(any())).thenReturn(expectedResponse);

        // When
        TimeSeriesDataResponse result = chartDataService.getTimeSeriesData(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForLineChart(any());
    }

    @Test
    void getTimeSeriesData_withEmptyData_shouldReturnEmptyResult() {
        // Given
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorIdAndCreatedAtBetween(1L, startDate, endDate))
                .thenReturn(Collections.emptyList());
        
        TimeSeriesDataResponse expectedResponse = new TimeSeriesDataResponse();
        when(formattingService.formatForLineChart(any())).thenReturn(expectedResponse);

        // When
        TimeSeriesDataResponse result = chartDataService.getTimeSeriesData(1L, startDate, endDate);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForLineChart(Collections.emptyList());
    }

    @Test
    void getTimeSeriesData_withNullIndicator_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getTimeSeriesData(null, startDate, endDate));
    }

    @Test
    void getTimeSeriesData_withInvalidDateRange_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getTimeSeriesData(1L, endDate, startDate));
    }

    @Test
    void getTimeSeriesData_withNonExistentIndicator_shouldThrowException() {
        // Given
        when(indicatorRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            chartDataService.getTimeSeriesData(1L, startDate, endDate));
    }

    @Test
    void getLocationComparisonData_withMultipleLocations_shouldReturnComparison() {
        // Given
        List<Long> locationIds = Arrays.asList(1L, 2L);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L))
                .thenReturn(Arrays.asList(testFactValue));
        
        LocationComparisonResponse expectedResponse = new LocationComparisonResponse();
        when(formattingService.formatForBarChart(any(), eq("location"))).thenReturn(expectedResponse);

        // When
        LocationComparisonResponse result = chartDataService.getLocationComparisonData(1L, locationIds);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForBarChart(any(), eq("location"));
    }

    @Test
    void getLocationComparisonData_withSingleLocation_shouldReturnSingleSeries() {
        // Given
        List<Long> locationIds = Arrays.asList(1L);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L))
                .thenReturn(Arrays.asList(testFactValue));
        
        LocationComparisonResponse expectedResponse = new LocationComparisonResponse();
        when(formattingService.formatForBarChart(any(), eq("location"))).thenReturn(expectedResponse);

        // When
        LocationComparisonResponse result = chartDataService.getLocationComparisonData(1L, locationIds);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForBarChart(any(), eq("location"));
    }

    @Test
    void getLocationComparisonData_withNullLocationIds_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getLocationComparisonData(1L, null));
    }

    @Test
    void getLocationComparisonData_withEmptyLocationIds_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getLocationComparisonData(1L, Collections.emptyList()));
    }

    @Test
    void getDimensionBreakdownData_withValidDimension_shouldReturnBreakdown() {
        // Given
        String dimensionType = "category";
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L))
                .thenReturn(Arrays.asList(testFactValue));
        
        DimensionBreakdownResponse expectedResponse = new DimensionBreakdownResponse();
        when(formattingService.formatForPieChart(any(), eq(dimensionType))).thenReturn(expectedResponse);

        // When
        DimensionBreakdownResponse result = chartDataService.getDimensionBreakdownData(1L, dimensionType);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForPieChart(any(), eq(dimensionType));
    }

    @Test
    void getDimensionBreakdownData_withInvalidDimension_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getDimensionBreakdownData(1L, ""));
    }

    @Test
    void getIndicatorCorrelationData_withTwoIndicators_shouldReturnCorrelation() {
        // Given
        List<Long> indicatorIds = Arrays.asList(1L, 2L);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L))
                .thenReturn(Arrays.asList(testFactValue));
        when(factIndicatorValueRepository.findByIndicatorId(2L))
                .thenReturn(Arrays.asList(testFactValue));
        
        CorrelationDataResponse expectedResponse = new CorrelationDataResponse();
        when(formattingService.formatForScatterPlot(any(), any())).thenReturn(expectedResponse);

        // When
        CorrelationDataResponse result = chartDataService.getIndicatorCorrelationData(indicatorIds);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForScatterPlot(any(), any());
    }

    @Test
    void getIndicatorCorrelationData_withSameIndicator_shouldThrowException() {
        // Given
        List<Long> indicatorIds = Arrays.asList(1L, 1L);
        // No stubbing needed

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getIndicatorCorrelationData(indicatorIds));
    }

    @Test
    void getIndicatorCorrelationData_withTooFewIndicators_shouldThrowException() {
        // Given
        List<Long> indicatorIds = Arrays.asList(1L);

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getIndicatorCorrelationData(indicatorIds));
    }

    @Test
    void getIndicatorCorrelationData_withTooManyIndicators_shouldThrowException() {
        // Given
        List<Long> indicatorIds = Arrays.asList(1L, 2L, 3L);

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getIndicatorCorrelationData(indicatorIds));
    }

    @Test
    void getTrendAnalysisData_withSufficientData_shouldReturnTrend() {
        // Given
        int periods = 5;
        List<FactIndicatorValue> data = Arrays.asList(testFactValue, testFactValue, testFactValue, testFactValue, testFactValue);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(data);
        
        TrendAnalysisResponse expectedResponse = new TrendAnalysisResponse();
        when(formattingService.calculateTrendAnalysis(any(), eq(periods))).thenReturn(expectedResponse);

        // When
        TrendAnalysisResponse result = chartDataService.getTrendAnalysisData(1L, periods);

        // Then
        assertNotNull(result);
        verify(formattingService).calculateTrendAnalysis(any(), eq(periods));
    }

    @Test
    void getTrendAnalysisData_withInsufficientData_shouldThrowException() {
        // Given
        int periods = 5;
        List<FactIndicatorValue> data = Arrays.asList(testFactValue, testFactValue);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(data);

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getTrendAnalysisData(1L, periods));
    }

    @Test
    void getTrendAnalysisData_withInvalidPeriods_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getTrendAnalysisData(1L, 0));
    }

    @Test
    void getGoalProgressChartData_withValidGoal_shouldReturnProgress() {
        // When
        TimeSeriesDataResponse result = chartDataService.getGoalProgressChartData(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getLabels());
        assertNotNull(result.getDatasets());
        assertNotNull(result.getChartConfig());
        assertEquals("Goal Progress", result.getChartConfig().getTitle());
    }

    @Test
    void getGoalProgressChartData_withInvalidGoalId_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getGoalProgressChartData(0L));
    }

    @Test
    void getSubareaPerformanceHeatmap_withValidArea_shouldReturnHeatmap() {
        // When
        HeatmapDataResponse result = chartDataService.getSubareaPerformanceHeatmap(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getXLabels());
        assertNotNull(result.getYLabels());
        assertNotNull(result.getData());
        assertNotNull(result.getColorScale());
        assertNotNull(result.getChartConfig());
        assertEquals("Subarea Performance Heatmap", result.getChartConfig().getTitle());
    }

    @Test
    void getSubareaPerformanceHeatmap_withInvalidAreaId_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.getSubareaPerformanceHeatmap(0L));
    }

    @Test
    void formatChartDataForType_LINE_shouldReturnLineFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        TimeSeriesDataResponse expectedResponse = new TimeSeriesDataResponse();
        when(formattingService.formatForLineChart(any())).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.LINE);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForLineChart(data);
    }

    @Test
    void formatChartDataForType_BAR_shouldReturnBarFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        LocationComparisonResponse expectedResponse = new LocationComparisonResponse();
        when(formattingService.formatForBarChart(any(), eq("category"))).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.BAR);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForBarChart(data, "category");
    }

    @Test
    void formatChartDataForType_PIE_shouldReturnPieFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        DimensionBreakdownResponse expectedResponse = new DimensionBreakdownResponse();
        when(formattingService.formatForPieChart(any(), eq("category"))).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.PIE);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForPieChart(data, "category");
    }

    @Test
    void formatChartDataForType_SCATTER_shouldReturnScatterFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        CorrelationDataResponse expectedResponse = new CorrelationDataResponse();
        when(formattingService.formatForScatterPlot(any(), any())).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.SCATTER);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForScatterPlot(data, data);
    }

    @Test
    void formatChartDataForType_AREA_shouldReturnAreaFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        TimeSeriesDataResponse expectedResponse = new TimeSeriesDataResponse();
        when(formattingService.formatForAreaChart(any())).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.AREA);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForAreaChart(data);
    }

    @Test
    void formatChartDataForType_HEATMAP_shouldReturnHeatmapFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        HeatmapDataResponse expectedResponse = new HeatmapDataResponse();
        when(formattingService.formatForHeatmap(any(), eq("xDimension"), eq("yDimension"))).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.HEATMAP);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForHeatmap(data, "xDimension", "yDimension");
    }

    @Test
    void formatChartDataForType_GAUGE_shouldReturnGaugeFormat() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);
        DimensionBreakdownResponse expectedResponse = new DimensionBreakdownResponse();
        when(formattingService.formatForGauge(any(), any())).thenReturn(expectedResponse);

        // When
        Object result = chartDataService.formatChartDataForType(data, VisualizationType.GAUGE);

        // Then
        assertNotNull(result);
        verify(formattingService).formatForGauge(testFactValue, null);
    }

    @Test
    void formatChartDataForType_withNullData_shouldThrowException() {
        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.formatChartDataForType(null, VisualizationType.LINE));
    }

    @Test
    void formatChartDataForType_withNullType_shouldThrowException() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            chartDataService.formatChartDataForType(data, null));
    }

    @Test
    void formatChartDataForType_withUnsupportedType_shouldThrowException() {
        // Given
        List<FactIndicatorValue> data = Arrays.asList(testFactValue);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            chartDataService.formatChartDataForType(data, VisualizationType.valueOf("UNSUPPORTED")));
    }
} 