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

    @Mock
    private GoalService goalService;

    @Mock
    private GoalGroupService goalGroupService;

    @Mock
    private GoalIndicatorService goalIndicatorService;

    @Mock
    private SubareaService subareaService;

    @Mock
    private SubareaIndicatorRepository subareaIndicatorRepository;

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
    private SubareaIndicator testSubareaIndicator;

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

        testSubareaIndicator = new SubareaIndicator();
        testSubareaIndicator.setSubarea(testSubarea);
        SubareaIndicator.SubareaIndicatorId subareaIndicatorId = new SubareaIndicator.SubareaIndicatorId();
        subareaIndicatorId.setSubareaId(1L);
        subareaIndicatorId.setIndicatorId(1L);
        testSubareaIndicator.setId(subareaIndicatorId);
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
    void getDashboardWithRelationships_ShouldReturnCompleteData() {
        // Given
        List<Area> areas = Arrays.asList(testArea);
        List<SubareaResponse> subareas = Arrays.asList(testSubareaResponse);
        List<GoalResponse> goals = Arrays.asList(testGoalResponse);
        List<GoalGroupResponse> goalGroups = Arrays.asList(testGoalGroupResponse);
        List<GoalIndicatorResponse> goalIndicators = Arrays.asList(testGoalIndicatorResponse);
        List<SubareaIndicator> subareaIndicators = Arrays.asList(testSubareaIndicator);

        when(areaRepository.findAll()).thenReturn(areas);
        when(subareaService.findAll()).thenReturn(subareas);
        when(goalService.findAll()).thenReturn(goals);
        when(goalGroupService.findAll()).thenReturn(goalGroups);
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(goalIndicators);
        when(subareaIndicatorRepository.findByIndicatorId(1L)).thenReturn(subareaIndicators);

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAreas().size());
        assertEquals(1, result.getSubareas().size());
        assertEquals(1, result.getGoals().size());
        assertEquals(1, result.getGoalGroups().size());
        assertNotNull(result.getRelationships());
        assertEquals(1, result.getRelationships().getGoalToSubareas().size());
        assertEquals(1, result.getRelationships().getSubareaToGoals().size());
        assertNotNull(result.getLastUpdated());
    }

    @Test
    void getDashboardWithRelationships_WithNoRelationships_ShouldReturnEmptyMappings() {
        // Given
        List<Area> areas = Arrays.asList(testArea);
        List<SubareaResponse> subareas = Arrays.asList(testSubareaResponse);
        List<GoalResponse> goals = Arrays.asList(testGoalResponse);
        List<GoalGroupResponse> goalGroups = Arrays.asList(testGoalGroupResponse);

        when(areaRepository.findAll()).thenReturn(areas);
        when(subareaService.findAll()).thenReturn(subareas);
        when(goalService.findAll()).thenReturn(goals);
        when(goalGroupService.findAll()).thenReturn(goalGroups);
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(Arrays.asList());

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAreas().size());
        assertEquals(1, result.getSubareas().size());
        assertEquals(1, result.getGoals().size());
        assertEquals(1, result.getGoalGroups().size());
        assertNotNull(result.getRelationships());
        assertEquals(1, result.getRelationships().getGoalToSubareas().size());
        assertEquals(0, result.getRelationships().getSubareaToGoals().size());
    }

    @Test
    void getDashboardWithRelationships_WithMultipleGoalsAndIndicators_ShouldBuildCorrectMappings() {
        // Given
        GoalResponse goal1 = GoalResponse.builder().id(1L).name("Goal 1").build();
        GoalResponse goal2 = GoalResponse.builder().id(2L).name("Goal 2").build();
        
        GoalIndicatorResponse indicator1 = new GoalIndicatorResponse();
        indicator1.setGoalId(1L);
        indicator1.setIndicatorId(1L);
        
        GoalIndicatorResponse indicator2 = new GoalIndicatorResponse();
        indicator2.setGoalId(1L);
        indicator2.setIndicatorId(2L);
        
        GoalIndicatorResponse indicator3 = new GoalIndicatorResponse();
        indicator3.setGoalId(2L);
        indicator3.setIndicatorId(1L);

        SubareaIndicator subareaIndicator1 = new SubareaIndicator();
        subareaIndicator1.setSubarea(testSubarea);
        
        SubareaIndicator subareaIndicator2 = new SubareaIndicator();
        Subarea subarea2 = new Subarea();
        subarea2.setId(2L);
        subareaIndicator2.setSubarea(subarea2);

        List<Area> areas = Arrays.asList(testArea);
        List<SubareaResponse> subareas = Arrays.asList(testSubareaResponse);
        List<GoalResponse> goals = Arrays.asList(goal1, goal2);
        List<GoalGroupResponse> goalGroups = Arrays.asList(testGoalGroupResponse);

        when(areaRepository.findAll()).thenReturn(areas);
        when(subareaService.findAll()).thenReturn(subareas);
        when(goalService.findAll()).thenReturn(goals);
        when(goalGroupService.findAll()).thenReturn(goalGroups);
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenReturn(Arrays.asList(indicator1, indicator2));
        when(goalIndicatorService.findIndicatorsByGoal(2L)).thenReturn(Arrays.asList(indicator3));
        when(subareaIndicatorRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(subareaIndicator1));
        when(subareaIndicatorRepository.findByIndicatorId(2L)).thenReturn(Arrays.asList(subareaIndicator2));

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(2, result.getRelationships().getGoalToSubareas().size());
        assertEquals(2, result.getRelationships().getSubareaToGoals().size());
        
        // Goal 1 should be connected to subareas 1 and 2
        List<String> goal1Subareas = result.getRelationships().getGoalToSubareas().get("1");
        assertEquals(2, goal1Subareas.size());
        assertTrue(goal1Subareas.contains("1"));
        assertTrue(goal1Subareas.contains("2"));
        
        // Goal 2 should be connected to subarea 1
        List<String> goal2Subareas = result.getRelationships().getGoalToSubareas().get("2");
        assertEquals(1, goal2Subareas.size());
        assertTrue(goal2Subareas.contains("1"));
        
        // Subarea 1 should be connected to goals 1 and 2
        List<String> subarea1Goals = result.getRelationships().getSubareaToGoals().get("1");
        assertEquals(2, subarea1Goals.size());
        assertTrue(subarea1Goals.contains("1"));
        assertTrue(subarea1Goals.contains("2"));
        
        // Subarea 2 should be connected to goal 1
        List<String> subarea2Goals = result.getRelationships().getSubareaToGoals().get("2");
        assertEquals(1, subarea2Goals.size());
        assertTrue(subarea2Goals.contains("1"));
    }

    @Test
    void getDashboardWithRelationships_WithExceptionInGoalIndicators_ShouldContinueProcessing() {
        // Given
        List<Area> areas = Arrays.asList(testArea);
        List<SubareaResponse> subareas = Arrays.asList(testSubareaResponse);
        List<GoalResponse> goals = Arrays.asList(testGoalResponse);
        List<GoalGroupResponse> goalGroups = Arrays.asList(testGoalGroupResponse);

        when(areaRepository.findAll()).thenReturn(areas);
        when(subareaService.findAll()).thenReturn(subareas);
        when(goalService.findAll()).thenReturn(goals);
        when(goalGroupService.findAll()).thenReturn(goalGroups);
        when(goalIndicatorService.findIndicatorsByGoal(1L)).thenThrow(new RuntimeException("Test exception"));

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getAreas().size());
        assertEquals(1, result.getSubareas().size());
        assertEquals(1, result.getGoals().size());
        assertEquals(1, result.getGoalGroups().size());
        assertNotNull(result.getRelationships());
        assertEquals(1, result.getRelationships().getGoalToSubareas().size());
        assertEquals(0, result.getRelationships().getSubareaToGoals().size());
    }

    @Test
    void getDashboardWithRelationships_WithEmptyData_ShouldReturnEmptyResponse() {
        // Given
        when(areaRepository.findAll()).thenReturn(Arrays.asList());
        when(subareaService.findAll()).thenReturn(Arrays.asList());
        when(goalService.findAll()).thenReturn(Arrays.asList());
        when(goalGroupService.findAll()).thenReturn(Arrays.asList());

        // When
        DashboardWithRelationshipsResponse result = dashboardDataService.getDashboardWithRelationships();

        // Then
        assertNotNull(result);
        assertEquals(0, result.getAreas().size());
        assertEquals(0, result.getSubareas().size());
        assertEquals(0, result.getGoals().size());
        assertEquals(0, result.getGoalGroups().size());
        assertNotNull(result.getRelationships());
        assertEquals(0, result.getRelationships().getGoalToSubareas().size());
        assertEquals(0, result.getRelationships().getSubareaToGoals().size());
    }
} 