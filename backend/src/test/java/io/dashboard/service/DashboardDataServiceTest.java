package io.dashboard.service;

import io.dashboard.dto.*;
import io.dashboard.model.*;
import io.dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardDataServiceTest {

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private DashboardWidgetRepository dashboardWidgetRepository;

    @Mock
    private PerformanceScoreRepository performanceScoreRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private SubareaRepository subareaRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private FactIndicatorValueRepository factIndicatorValueRepository;

    @InjectMocks
    private DashboardDataService dashboardDataService;

    private Dashboard testDashboard;
    private DashboardWidget testWidget;
    private PerformanceScore testPerformanceScore;
    private Subarea testSubarea;
    private Area testArea;
    private Indicator testIndicator;
    private FactIndicatorValue testFactValue;

    @BeforeEach
    void setUp() {
        testDashboard = new Dashboard();
        testDashboard.setId(1L);
        testDashboard.setName("Test Dashboard");
        testDashboard.setDescription("Test Description");
        testDashboard.setLayoutType(LayoutType.GRID);

        testWidget = new DashboardWidget();
        testWidget.setId(1L);
        testWidget.setDashboardId(1L);
        testWidget.setTitle("Test Widget");
        testWidget.setWidgetType(WidgetType.AREA);
        testWidget.setPositionX(0);
        testWidget.setPositionY(0);
        testWidget.setWidth(2);
        testWidget.setHeight(2);
        testWidget.setConfig("{\"key\":\"value\"}");

        testPerformanceScore = new PerformanceScore();
        testPerformanceScore.setId(1L);
        testPerformanceScore.setSubareaId(1L);
        testPerformanceScore.setScore(85.0);
        testPerformanceScore.setColorCode("#00FF00");
        testPerformanceScore.setCalculatedAt(LocalDateTime.now());
        testPerformanceScore.setBasedOnIndicators("1,2,3");

        testSubarea = new Subarea();
        testSubarea.setId(1L);
        testSubarea.setName("Test Subarea");

        testArea = new Area();
        testArea.setId(1L);
        testArea.setName("Test Area");

        testIndicator = new Indicator();
        testIndicator.setId(1L);
        testIndicator.setCode("TEST001");
        testIndicator.setName("Test Indicator");
        testIndicator.setDescription("Test Description");
        testIndicator.setIsComposite(false);

        testFactValue = new FactIndicatorValue();
        testFactValue.setId(1L);
        testFactValue.setValue(new BigDecimal("85.0"));
        DimTime time = new DimTime();
        time.setValue("2023-01-01");
        testFactValue.setTime(time);
    }

    @Test
    void getDashboardData_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenReturn(Arrays.asList(testWidget));

        // When
        DashboardDataResponse result = dashboardDataService.getDashboardData(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getLastUpdated());
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findByDashboardId(1L);
    }

    @Test
    void getDashboardData_DashboardNotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDashboardData(1L);
        });
    }

    @Test
    void getWidgetData_AreaWidget() {
        // Given
        testWidget.setWidgetType(WidgetType.AREA);

        // When
        WidgetDataResponse result = dashboardDataService.getWidgetData(testWidget);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getWidgetId());
        assertEquals(WidgetType.AREA, result.getWidgetType());
        assertEquals("Test Widget", result.getTitle());
        assertEquals("0,0", result.getPosition());
        assertEquals("2x2", result.getSize());
        assertEquals("{\"key\":\"value\"}", result.getConfig());
    }

    @Test
    void getWidgetData_SubareaWidget() {
        // Given
        testWidget.setWidgetType(WidgetType.SUBAREA);

        // When
        WidgetDataResponse result = dashboardDataService.getWidgetData(testWidget);

        // Then
        assertNotNull(result);
        assertEquals(WidgetType.SUBAREA, result.getWidgetType());
    }

    @Test
    void getWidgetData_IndicatorWidget() {
        // Given
        testWidget.setWidgetType(WidgetType.INDICATOR);

        // When
        WidgetDataResponse result = dashboardDataService.getWidgetData(testWidget);

        // Then
        assertNotNull(result);
        assertEquals(WidgetType.INDICATOR, result.getWidgetType());
    }

    @Test
    void getWidgetData_GoalWidget() {
        // Given
        testWidget.setWidgetType(WidgetType.GOAL);

        // When
        WidgetDataResponse result = dashboardDataService.getWidgetData(testWidget);

        // Then
        assertNotNull(result);
        assertEquals(WidgetType.GOAL, result.getWidgetType());
    }

    @Test
    void getPerformanceMetrics_Success() {
        // Given
        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));

        // When
        PerformanceMetricsResponse result = dashboardDataService.getPerformanceMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAreaId());
        assertEquals(1, result.getMetrics().size());
        assertEquals(85.0, result.getAverageScore());
        verify(subareaRepository).findByAreaId(1L);
        verify(performanceScoreRepository).findBySubareaId(1L);
    }

    @Test
    void getPerformanceMetrics_NoSubareas() {
        // Given
        when(subareaRepository.findByAreaId(1L)).thenReturn(Collections.emptyList());

        // When
        PerformanceMetricsResponse result = dashboardDataService.getPerformanceMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAreaId());
        assertTrue(result.getMetrics().isEmpty());
        assertEquals(0.0, result.getAverageScore());
    }

    @Test
    void getPerformanceMetrics_NoPerformanceScores() {
        // Given
        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Collections.emptyList());

        // When
        PerformanceMetricsResponse result = dashboardDataService.getPerformanceMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getAreaId());
        assertTrue(result.getMetrics().isEmpty());
        assertEquals(0.0, result.getAverageScore());
    }

    @Test
    void getDataAggregation_Sum() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "SUM", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
        assertEquals("SUM", result.getAggregationType());
        assertEquals(85.0, result.getData().get("value"));
        assertEquals(1, result.getData().get("count"));
    }

    @Test
    void getDataAggregation_Average() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "AVERAGE", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals("AVERAGE", result.getAggregationType());
        assertEquals(85.0, result.getData().get("value"));
    }

    @Test
    void getDataAggregation_Max() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "MAX", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals("MAX", result.getAggregationType());
        assertEquals(85.0, result.getData().get("value"));
    }

    @Test
    void getDataAggregation_Min() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "MIN", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals("MIN", result.getAggregationType());
        assertEquals(85.0, result.getData().get("value"));
    }

    @Test
    void getDataAggregation_UnknownType() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "UNKNOWN", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals("UNKNOWN", result.getAggregationType());
        assertEquals(0.0, result.getData().get("value"));
    }

    @Test
    void getDataAggregation_EmptyData() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "SUM", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getData().get("value"));
        assertEquals(0, result.getData().get("count"));
    }

    @Test
    void getRealTimeUpdates_Success() {
        // Given
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenReturn(Arrays.asList(testWidget));

        // When
        RealTimeUpdateResponse result = dashboardDataService.getRealTimeUpdates(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDashboardId());
        assertEquals(1, result.getUpdates().size());
        assertNotNull(result.getTimestamp());
        verify(dashboardWidgetRepository).findByDashboardId(1L);
    }

    @Test
    void getRealTimeUpdates_NoWidgets() {
        // Given
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenReturn(Collections.emptyList());

        // When
        RealTimeUpdateResponse result = dashboardDataService.getRealTimeUpdates(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDashboardId());
        assertTrue(result.getUpdates().isEmpty());
    }

    @Test
    void getHistoricalData_Success() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        HistoricalDataResponse result = dashboardDataService.getHistoricalData(1L, 6);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
        assertEquals(1, result.getDataPoints().size());
        assertNotNull(result.getStartDate());
        assertNotNull(result.getEndDate());
        verify(factIndicatorValueRepository).findByIndicatorId(1L);
    }

    @Test
    void getHistoricalData_EmptyData() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Collections.emptyList());

        // When
        HistoricalDataResponse result = dashboardDataService.getHistoricalData(1L, 6);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
        assertTrue(result.getDataPoints().isEmpty());
    }

    @Test
    void getDataValidation_Success() {
        // Given
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataValidationResponse result = dashboardDataService.getDataValidation(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
        assertNotNull(result.getIsValid());
        assertNotNull(result.getValidationErrors());
        verify(indicatorRepository).findById(1L);
    }

    @Test
    void getDataValidation_IndicatorNotFound() {
        // Given
        when(indicatorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDataValidation(1L);
        });
    }

    @Test
    void getDataValidation_RepositoryException() {
        // Given
        when(indicatorRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDataValidation(1L);
        });
    }

    @Test
    void getDataQualityMetrics_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenReturn(Arrays.asList(testWidget));

        // When
        DataQualityMetricsResponse result = dashboardDataService.getDataQualityMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDashboardId());
        assertNotNull(result.getCompleteness());
        assertNotNull(result.getAccuracy());
        assertNotNull(result.getTimeliness());
        verify(dashboardRepository).findById(1L);
        verify(dashboardWidgetRepository).findByDashboardId(1L);
    }

    @Test
    void getDataRefreshStatus_Success() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.of(testDashboard));

        // When
        DataRefreshStatusResponse result = dashboardDataService.getDataRefreshStatus(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDashboardId());
        assertNotNull(result.getLastRefresh());
        assertNotNull(result.getStatus());
        verify(dashboardRepository).findById(1L);
    }

    @Test
    void getPerformanceMetrics_MultipleSubareas() {
        // Given
        Subarea subarea2 = new Subarea();
        subarea2.setId(2L);
        subarea2.setName("Test Subarea 2");

        PerformanceScore score2 = new PerformanceScore();
        score2.setId(2L);
        score2.setSubareaId(2L);
        score2.setScore(90.0);
        score2.setColorCode("#00FF00");
        score2.setCalculatedAt(LocalDateTime.now());
        score2.setBasedOnIndicators("4,5,6");

        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea, subarea2));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));
        when(performanceScoreRepository.findBySubareaId(2L)).thenReturn(Arrays.asList(score2));

        // When
        PerformanceMetricsResponse result = dashboardDataService.getPerformanceMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getMetrics().size());
        assertEquals(87.5, result.getAverageScore()); // (85.0 + 90.0) / 2
    }

    @Test
    void getDataAggregation_MultipleValues() {
        // Given
        FactIndicatorValue value2 = new FactIndicatorValue();
        value2.setId(2L);
        value2.setValue(new BigDecimal("90.0"));
        DimTime time2 = new DimTime();
        time2.setValue("2023-01-02");
        value2.setTime(time2);

        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue, value2));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "SUM", LocalDateTime.now(), LocalDateTime.now());

        // Then
        assertNotNull(result);
        assertEquals(175.0, result.getData().get("value")); // 85.0 + 90.0
        assertEquals(2, result.getData().get("count"));
    }

    @Test
    void getHistoricalData_MultipleDataPoints() {
        // Given
        FactIndicatorValue value2 = new FactIndicatorValue();
        value2.setId(2L);
        value2.setValue(new BigDecimal("90.0"));
        DimTime time2 = new DimTime();
        time2.setValue("2023-01-02");
        value2.setTime(time2);

        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue, value2));

        // When
        HistoricalDataResponse result = dashboardDataService.getHistoricalData(1L, 6);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getDataPoints().size());
    }

    @Test
    void getDataRefreshStatus_DashboardNotFound() {
        // Given
        when(dashboardRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        DataRefreshStatusResponse result = dashboardDataService.getDataRefreshStatus(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getDashboardId());
        assertEquals("NOT_FOUND", result.getStatus());
        assertNotNull(result.getLastRefresh());
    }

    @Test
    void getPerformanceMetrics_RepositoryException() {
        // Given
        when(subareaRepository.findByAreaId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getPerformanceMetrics(1L);
        });
    }

    @Test
    void getDataAggregation_RepositoryException() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDataAggregation(1L, "SUM", LocalDateTime.now(), LocalDateTime.now());
        });
    }

    @Test
    void getRealTimeUpdates_RepositoryException() {
        // Given
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getRealTimeUpdates(1L);
        });
    }

    @Test
    void getHistoricalData_RepositoryException() {
        // Given
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getHistoricalData(1L, 6);
        });
    }

    @Test
    void getDataQualityMetrics_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDataQualityMetrics(1L);
        });
    }

    @Test
    void getDataRefreshStatus_RepositoryException() {
        // Given
        when(dashboardRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            dashboardDataService.getDataRefreshStatus(1L);
        });
    }
} 