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

    @Mock
    private GoalService goalService;

    @Mock
    private GoalGroupService goalGroupService;

    @Mock
    private GoalIndicatorService goalIndicatorService;

    @Mock
    private SubareaService subareaService;

    @InjectMocks
    private DashboardDataService dashboardDataService;

    private Dashboard testDashboard;
    private DashboardWidget testWidget;
    private PerformanceScore testPerformanceScore;
    private Subarea testSubarea;
    private Area testArea;
    private Indicator testIndicator;
    private FactIndicatorValue testFactValue;
    private SubareaResponse testSubareaResponse;
    private GoalResponse testGoalResponse;
    private GoalGroupResponse testGoalGroupResponse;
    private GoalIndicatorResponse testGoalIndicatorResponse;

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

        // Setup test data for relationships
        testSubareaResponse = new SubareaResponse();
        testSubareaResponse.setId(1L);
        testSubareaResponse.setName("Test Subarea");
        testSubareaResponse.setDescription("Test Subarea Description");
        testSubareaResponse.setCode("TEST_SUB");
        testSubareaResponse.setAreaId(1L);
        testSubareaResponse.setAreaName("Test Area");

        testGoalResponse = GoalResponse.builder()
                .id(1L)
                .name("Test Goal")
                .description("Test Goal Description")
                .type("quantitative")
                .year(2024)
                .createdAt(LocalDateTime.now())
                .build();

        testGoalGroupResponse = GoalGroupResponse.builder()
                .id(1L)
                .name("Test Goal Group")
                .description("Test Goal Group Description")
                .createdAt(LocalDateTime.now())
                .build();

        testGoalIndicatorResponse = new GoalIndicatorResponse();
        testGoalIndicatorResponse.setGoalId(1L);
        testGoalIndicatorResponse.setIndicatorId(1L);
        testGoalIndicatorResponse.setGoalName("Test Goal");
        testGoalIndicatorResponse.setIndicatorName("Test Indicator");
        testGoalIndicatorResponse.setIndicatorCode("TEST_IND");
        testGoalIndicatorResponse.setAggregationWeight(1.0);
        testGoalIndicatorResponse.setImpactDirection(ImpactDirection.POSITIVE);
        testGoalIndicatorResponse.setCreatedAt(LocalDateTime.now());
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
        assertEquals(WidgetType.AREA, result.getWidgetType());
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
    }

    @Test
    void getRealTimeUpdates_Success() {
        // Given
        when(dashboardWidgetRepository.findByDashboardId(1L)).thenReturn(Arrays.asList(testWidget));

        // When
        RealTimeUpdateResponse result = dashboardDataService.getRealTimeUpdates(1L);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUpdates());
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
        assertTrue(result.getUpdates().isEmpty());
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
        assertTrue(result.getIsValid());
        verify(indicatorRepository).findById(1L);
        verify(factIndicatorValueRepository).findByIndicatorId(1L);
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
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenThrow(new RuntimeException("Database error"));

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
        assertNotNull(result.getCompleteness());
        assertNotNull(result.getAccuracy());
        assertNotNull(result.getTimeliness());
        assertNotNull(result.getOverallScore());
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

        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea, subarea2));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));
        when(performanceScoreRepository.findBySubareaId(2L)).thenReturn(Arrays.asList(score2));

        // When
        PerformanceMetricsResponse result = dashboardDataService.getPerformanceMetrics(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getMetrics().size());
        verify(subareaRepository).findByAreaId(1L);
        verify(performanceScoreRepository).findBySubareaId(1L);
        verify(performanceScoreRepository).findBySubareaId(2L);
    }

    @Test
    void getDataAggregation_MultipleValues() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        when(factIndicatorValueRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DataAggregationResponse result = dashboardDataService.getDataAggregation(1L, "AVERAGE", startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIndicatorId());
        assertEquals("AVERAGE", result.getAggregationType());
        assertNotNull(result.getData());
    }

    @Test
    void getDashboardWithRelationships_ShouldReturnCompleteData() {
        // Given
        when(areaRepository.findAll()).thenReturn(Arrays.asList(testArea));
        when(subareaService.findAll()).thenReturn(Arrays.asList(testSubareaResponse));
        when(goalService.findAll()).thenReturn(Arrays.asList(testGoalResponse));
        when(goalGroupService.findAll()).thenReturn(Arrays.asList(testGoalGroupResponse));
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(Arrays.asList(testGoalIndicatorResponse));
        when(factIndicatorValueRepository.findByIndicatorIdWithSubarea(1L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertNotNull(result.getAreas());
        assertNotNull(result.getSubareas());
        assertNotNull(result.getGoals());
        assertNotNull(result.getGoalGroups());
        assertNotNull(result.getRelationships());
        verify(areaRepository).findAll();
        verify(subareaService).findAll();
        verify(goalService).findAll();
        verify(goalGroupService).findAll();
    }

    @Test
    void getDashboardWithRelationships_WithNoRelationships_ShouldReturnEmptyMappings() {
        // Given
        when(areaRepository.findAll()).thenReturn(Collections.emptyList());
        when(subareaService.findAll()).thenReturn(Collections.emptyList());
        when(goalService.findAll()).thenReturn(Collections.emptyList());
        when(goalGroupService.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertTrue(result.getAreas().isEmpty());
        assertTrue(result.getSubareas().isEmpty());
        assertTrue(result.getGoals().isEmpty());
        assertTrue(result.getGoalGroups().isEmpty());
    }

    @Test
    void getDashboardWithRelationships_WithMultipleGoalsAndIndicators_ShouldBuildCorrectMappings() {
        // Given
        GoalResponse goal2 = GoalResponse.builder()
                .id(2L)
                .name("Test Goal 2")
                .description("Test Goal 2 Description")
                .type("quantitative")
                .year(2024)
                .createdAt(LocalDateTime.now())
                .build();

        GoalIndicatorResponse goalIndicator2 = new GoalIndicatorResponse();
        goalIndicator2.setGoalId(2L);
        goalIndicator2.setIndicatorId(2L);
        goalIndicator2.setGoalName("Test Goal 2");
        goalIndicator2.setIndicatorName("Test Indicator 2");
        goalIndicator2.setIndicatorCode("TEST_IND_2");
        goalIndicator2.setAggregationWeight(0.5);
        goalIndicator2.setImpactDirection(ImpactDirection.NEGATIVE);
        goalIndicator2.setCreatedAt(LocalDateTime.now());

        Indicator indicator2 = new Indicator();
        indicator2.setId(2L);
        indicator2.setCode("TEST002");
        indicator2.setName("Test Indicator 2");

        when(areaRepository.findAll()).thenReturn(Arrays.asList(testArea));
        when(subareaService.findAll()).thenReturn(Arrays.asList(testSubareaResponse));
        when(goalService.findAll()).thenReturn(Arrays.asList(testGoalResponse, goal2));
        when(goalGroupService.findAll()).thenReturn(Arrays.asList(testGoalGroupResponse));
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(Arrays.asList(testGoalIndicatorResponse));
        when(goalIndicatorService.findIndicatorsByGoal(2L)).thenReturn(Arrays.asList(goalIndicator2));
        when(factIndicatorValueRepository.findByIndicatorIdWithSubarea(1L)).thenReturn(Arrays.asList(testFactValue));
        when(factIndicatorValueRepository.findByIndicatorIdWithSubarea(2L)).thenReturn(Arrays.asList(testFactValue));

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAreas().size());
        assertEquals(1, result.getSubareas().size());
        assertEquals(2, result.getGoals().size());
        assertEquals(1, result.getGoalGroups().size());
        assertNotNull(result.getRelationships());
    }

    @Test
    void getDashboardWithRelationships_WithExceptionInGoalIndicators_ShouldContinueProcessing() {
        // Given
        when(areaRepository.findAll()).thenReturn(Arrays.asList(testArea));
        when(subareaService.findAll()).thenReturn(Arrays.asList(testSubareaResponse));
        when(goalService.findAll()).thenReturn(Arrays.asList(testGoalResponse));
        when(goalGroupService.findAll()).thenReturn(Arrays.asList(testGoalGroupResponse));
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenThrow(new RuntimeException("Service error"));

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAreas().size());
        assertEquals(1, result.getSubareas().size());
        assertEquals(1, result.getGoals().size());
        assertEquals(1, result.getGoalGroups().size());
        verify(areaRepository).findAll();
        verify(subareaService).findAll();
        verify(goalService).findAll();
        verify(goalGroupService).findAll();
    }

    @Test
    void getDashboardWithRelationships_WithEmptyData_ShouldReturnEmptyResponse() {
        // Given
        when(areaRepository.findAll()).thenReturn(Collections.emptyList());
        when(subareaService.findAll()).thenReturn(Collections.emptyList());
        when(goalService.findAll()).thenReturn(Collections.emptyList());
        when(goalGroupService.findAll()).thenReturn(Collections.emptyList());

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertTrue(result.getAreas().isEmpty());
        assertTrue(result.getSubareas().isEmpty());
        assertTrue(result.getGoals().isEmpty());
        assertTrue(result.getGoalGroups().isEmpty());
    }
} 