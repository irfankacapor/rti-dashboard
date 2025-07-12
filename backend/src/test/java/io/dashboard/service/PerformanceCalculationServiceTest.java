package io.dashboard.service;

import io.dashboard.model.*;
import io.dashboard.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceCalculationServiceTest {

    @Mock
    private PerformanceScoreRepository performanceScoreRepository;

    @Mock
    private ColorThresholdRepository colorThresholdRepository;

    @Mock
    private SubareaRepository subareaRepository;

    @Mock
    private AreaRepository areaRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @Mock
    private SubareaIndicatorRepository subareaIndicatorRepository;

    @InjectMocks
    private PerformanceCalculationService performanceCalculationService;

    private Subarea testSubarea;
    private Area testArea;
    private Indicator testIndicator;
    private SubareaIndicator testSubareaIndicator;
    private ColorThreshold testColorThreshold;
    private PerformanceScore testPerformanceScore;

    @BeforeEach
    void setUp() {
        // Setup test data
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

        testSubareaIndicator = new SubareaIndicator();
        testSubareaIndicator.setId(new SubareaIndicator.SubareaIndicatorId(1L, 1L));
        testSubareaIndicator.setSubarea(testSubarea);
        testSubareaIndicator.setIndicator(testIndicator);
        testSubareaIndicator.setAggregationWeight(1.0);

        testColorThreshold = new ColorThreshold();
        testColorThreshold.setId(1L);
        testColorThreshold.setMinValue(80.0);
        testColorThreshold.setMaxValue(100.0);
        testColorThreshold.setColorCode("#00FF00");
        testColorThreshold.setDescription("Green");

        testPerformanceScore = new PerformanceScore();
        testPerformanceScore.setId(1L);
        testPerformanceScore.setSubareaId(1L);
        testPerformanceScore.setScore(85.0);
        testPerformanceScore.setColorCode("#00FF00");
        testPerformanceScore.setCalculatedAt(LocalDateTime.now());
        testPerformanceScore.setBasedOnIndicators("1,2,3");
    }

    @Test
    void calculateSubareaPerformance_Success() {
        // Given
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testSubareaIndicator));
        when(performanceScoreRepository.save(any(PerformanceScore.class))).thenReturn(testPerformanceScore);

        // When
        PerformanceScore result = performanceCalculationService.calculateSubareaPerformance(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getSubareaId());
        assertEquals(85.0, result.getScore());
        assertEquals("#00FF00", result.getColorCode());
        verify(performanceScoreRepository).save(any(PerformanceScore.class));
    }

    @Test
    void calculateSubareaPerformance_SubareaNotFound() {
        // Given
        when(subareaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateSubareaPerformance(1L);
        });
    }

    @Test
    void calculateSubareaPerformance_NoIndicators() {
        // Given
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateSubareaPerformance(1L);
        });
    }

    @Test
    void calculateAreaPerformance_Success() {
        // Given
        when(areaRepository.findById(1L)).thenReturn(Optional.of(testArea));
        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));
        when(colorThresholdRepository.findAll()).thenReturn(Arrays.asList(testColorThreshold));

        // When
        PerformanceScore result = performanceCalculationService.calculateAreaPerformance(1L);

        // Then
        assertNotNull(result);
        assertEquals(85.0, result.getScore());
        assertEquals("#00FF00", result.getColorCode());
        verify(areaRepository).findById(1L);
        verify(subareaRepository).findByAreaId(1L);
        verify(performanceScoreRepository).findBySubareaId(1L);
        verify(colorThresholdRepository).findAll();
    }

    @Test
    void calculateAreaPerformance_AreaNotFound() {
        // Given
        when(areaRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateAreaPerformance(1L);
        });
    }

    @Test
    void calculateAreaPerformance_NoSubareas() {
        // Given
        when(areaRepository.findById(1L)).thenReturn(Optional.of(testArea));
        when(subareaRepository.findByAreaId(1L)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateAreaPerformance(1L);
        });
    }

    @Test
    void getColorCodeForScore_Green() {
        // Given
        when(colorThresholdRepository.findAll()).thenReturn(Arrays.asList(testColorThreshold));

        // When
        String result = performanceCalculationService.getColorCodeForScore(85.0);

        // Then
        assertEquals("#00FF00", result);
    }

    @Test
    void getColorCodeForScore_DefaultColor() {
        // Given
        when(colorThresholdRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        String result = performanceCalculationService.getColorCodeForScore(85.0);

        // Then
        assertEquals("#000000", result);
    }

    @Test
    void updateColorThresholds_Success() {
        // Given
        List<ColorThreshold> thresholds = Arrays.asList(testColorThreshold);

        // When
        performanceCalculationService.updateColorThresholds(thresholds);

        // Then
        verify(colorThresholdRepository).deleteAll();
        verify(colorThresholdRepository).saveAll(thresholds);
    }

    @Test
    void getPerformanceHistory_Success() {
        // Given
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));

        // When
        List<PerformanceScore> result = performanceCalculationService.getPerformanceHistory(1L, 6);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPerformanceScore, result.get(0));
    }

    @Test
    void calculateAllPerformances_Success() {
        // Given
        when(subareaRepository.findAll()).thenReturn(Arrays.asList(testSubarea));
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testSubareaIndicator));
        when(performanceScoreRepository.save(any(PerformanceScore.class))).thenReturn(testPerformanceScore);

        // When
        performanceCalculationService.calculateAllPerformances();

        // Then
        verify(subareaRepository).findAll();
        verify(subareaRepository).findById(1L);
        verify(subareaIndicatorRepository).findBySubareaId(1L);
        verify(performanceScoreRepository).save(any(PerformanceScore.class));
    }

    @Test
    void calculateSubareaPerformance_WithNullIndicator() {
        // Given
        SubareaIndicator nullIndicator = new SubareaIndicator();
        nullIndicator.setId(new SubareaIndicator.SubareaIndicatorId(1L, 1L));
        nullIndicator.setSubarea(testSubarea);
        nullIndicator.setIndicator(null);
        nullIndicator.setAggregationWeight(1.0);

        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(nullIndicator));
        when(performanceScoreRepository.save(any(PerformanceScore.class))).thenReturn(testPerformanceScore);

        // When
        PerformanceScore result = performanceCalculationService.calculateSubareaPerformance(1L);

        // Then
        assertNotNull(result);
        verify(performanceScoreRepository).save(any(PerformanceScore.class));
    }

    @Test
    void calculateSubareaPerformance_WithNullWeight() {
        // Given
        SubareaIndicator nullWeight = new SubareaIndicator();
        nullWeight.setId(new SubareaIndicator.SubareaIndicatorId(1L, 1L));
        nullWeight.setSubarea(testSubarea);
        nullWeight.setIndicator(testIndicator);
        nullWeight.setAggregationWeight(null);

        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(nullWeight));
        when(performanceScoreRepository.save(any(PerformanceScore.class))).thenReturn(testPerformanceScore);

        // When
        PerformanceScore result = performanceCalculationService.calculateSubareaPerformance(1L);

        // Then
        assertNotNull(result);
        verify(performanceScoreRepository).save(any(PerformanceScore.class));
    }

    @Test
    void calculateSubareaPerformance_ScoreClamping() {
        // Given
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(testSubarea));
        when(subareaIndicatorRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testSubareaIndicator));
        when(performanceScoreRepository.save(any(PerformanceScore.class))).thenReturn(testPerformanceScore);

        // When
        PerformanceScore result = performanceCalculationService.calculateSubareaPerformance(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.getScore() >= 0.0);
        assertTrue(result.getScore() <= 100.0);
    }

    @Test
    void calculateAreaPerformance_WithMultipleSubareas() {
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

        when(areaRepository.findById(1L)).thenReturn(Optional.of(testArea));
        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea, subarea2));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(testPerformanceScore));
        when(performanceScoreRepository.findBySubareaId(2L)).thenReturn(Arrays.asList(score2));

        // When
        PerformanceScore result = performanceCalculationService.calculateAreaPerformance(1L);

        // Then
        assertNotNull(result);
        assertEquals(87.5, result.getScore()); // (85.0 + 90.0) / 2
    }

    @Test
    void calculateAreaPerformance_WithNoPerformanceScores() {
        // Given
        when(areaRepository.findById(1L)).thenReturn(Optional.of(testArea));
        when(subareaRepository.findByAreaId(1L)).thenReturn(Arrays.asList(testSubarea));
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Collections.emptyList());

        // When
        PerformanceScore result = performanceCalculationService.calculateAreaPerformance(1L);

        // Then
        assertNotNull(result);
        assertEquals(0.0, result.getScore());
    }

    @Test
    void getColorCodeForScore_MultipleThresholds() {
        // Given
        ColorThreshold redThreshold = new ColorThreshold();
        redThreshold.setId(2L);
        redThreshold.setMinValue(0.0);
        redThreshold.setMaxValue(50.0);
        redThreshold.setColorCode("#FF0000");
        redThreshold.setDescription("Red");

        ColorThreshold yellowThreshold = new ColorThreshold();
        yellowThreshold.setId(3L);
        yellowThreshold.setMinValue(50.0);
        yellowThreshold.setMaxValue(80.0);
        yellowThreshold.setColorCode("#FFFF00");
        yellowThreshold.setDescription("Yellow");

        when(colorThresholdRepository.findAll()).thenReturn(Arrays.asList(redThreshold, yellowThreshold, testColorThreshold));

        // When
        String redResult = performanceCalculationService.getColorCodeForScore(25.0);
        String yellowResult = performanceCalculationService.getColorCodeForScore(65.0);
        String greenResult = performanceCalculationService.getColorCodeForScore(85.0);

        // Then
        assertEquals("#FF0000", redResult);
        assertEquals("#FFFF00", yellowResult);
        assertEquals("#00FF00", greenResult);
    }

    @Test
    void getColorCodeForScore_BoundaryValues() {
        // Given
        when(colorThresholdRepository.findAll()).thenReturn(Arrays.asList(testColorThreshold));

        // When
        String minResult = performanceCalculationService.getColorCodeForScore(80.0);
        String maxResult = performanceCalculationService.getColorCodeForScore(100.0);

        // Then
        assertEquals("#00FF00", minResult);
        assertEquals("#00FF00", maxResult);
    }

    @Test
    void getColorCodeForScore_OutsideThresholds() {
        // Given
        when(colorThresholdRepository.findAll()).thenReturn(Arrays.asList(testColorThreshold));

        // When
        String belowResult = performanceCalculationService.getColorCodeForScore(75.0);
        String aboveResult = performanceCalculationService.getColorCodeForScore(105.0);

        // Then
        assertEquals("#000000", belowResult);
        assertEquals("#000000", aboveResult);
    }

    @Test
    void updateColorThresholds_EmptyList() {
        // Given
        List<ColorThreshold> thresholds = Collections.emptyList();

        // When
        performanceCalculationService.updateColorThresholds(thresholds);

        // Then
        verify(colorThresholdRepository).deleteAll();
        verify(colorThresholdRepository).saveAll(thresholds);
    }

    @Test
    void getPerformanceHistory_EmptyHistory() {
        // Given
        when(performanceScoreRepository.findBySubareaId(1L)).thenReturn(Collections.emptyList());

        // When
        List<PerformanceScore> result = performanceCalculationService.getPerformanceHistory(1L, 6);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateAllPerformances_EmptySubareas() {
        // Given
        when(subareaRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        performanceCalculationService.calculateAllPerformances();

        // Then
        verify(subareaRepository).findAll();
        verify(subareaIndicatorRepository, never()).findBySubareaId(any());
        verify(performanceScoreRepository, never()).save(any());
    }

    @Test
    void calculateSubareaPerformance_RepositoryException() {
        // Given
        when(subareaRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateSubareaPerformance(1L);
        });
    }

    @Test
    void calculateAreaPerformance_RepositoryException() {
        // Given
        when(areaRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateAreaPerformance(1L);
        });
    }

    @Test
    void getColorCodeForScore_RepositoryException() {
        // Given
        when(colorThresholdRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.getColorCodeForScore(85.0);
        });
    }

    @Test
    void updateColorThresholds_RepositoryException() {
        // Given
        List<ColorThreshold> thresholds = Arrays.asList(testColorThreshold);
        doThrow(new RuntimeException("Database error")).when(colorThresholdRepository).deleteAll();

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.updateColorThresholds(thresholds);
        });
    }

    @Test
    void getPerformanceHistory_RepositoryException() {
        // Given
        when(performanceScoreRepository.findBySubareaId(1L)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.getPerformanceHistory(1L, 6);
        });
    }

    @Test
    void calculateAllPerformances_RepositoryException() {
        // Given
        when(subareaRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            performanceCalculationService.calculateAllPerformances();
        });
    }
} 