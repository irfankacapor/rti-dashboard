package io.dashboard.service;

import io.dashboard.dto.GoalProgressResponse;
import io.dashboard.dto.IndicatorProgressItem;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalIndicator;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.ImpactDirection;
import io.dashboard.model.Indicator;
import io.dashboard.model.Unit;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.IndicatorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalIndicatorServiceBusinessLogicTest {

    @Mock
    private GoalIndicatorRepository goalIndicatorRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private GoalIndicatorService goalIndicatorService;

    private Goal goal;
    private Indicator indicator1;
    private Indicator indicator2;
    private GoalGroup goalGroup;
    private Unit unit;

    @BeforeEach
    void setUp() {
        goalGroup = new GoalGroup();
        goalGroup.setId(1L);
        goalGroup.setName("SDGs");

        goal = new Goal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setGoalGroup(goalGroup);

        unit = new Unit();
        unit.setId(1L);
        unit.setCode("PERCENT");

        indicator1 = new Indicator();
        indicator1.setId(1L);
        indicator1.setName("Test Indicator 1");
        indicator1.setCode("TEST_IND_1");
        indicator1.setUnitPrefix("€");
        indicator1.setUnitSuffix("thousand");

        indicator2 = new Indicator();
        indicator2.setId(2L);
        indicator2.setName("Test Indicator 2");
        indicator2.setCode("TEST_IND_2");
        indicator2.setUnitPrefix("€");
        indicator2.setUnitSuffix("thousand");
    }

    @Test
    void calculateGoalProgress_shouldCalculateCorrectly_withPositiveImpact() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(0.6);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator2);
        gi2.setAggregationWeight(0.4);
        gi2.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(2, result.getIndicatorProgress().size());
        
        // Verify progress calculation logic
        List<IndicatorProgressItem> progressItems = result.getIndicatorProgress();
        assertEquals(0.6, progressItems.get(0).getWeight());
        assertEquals(0.4, progressItems.get(1).getWeight());
        assertEquals(ImpactDirection.POSITIVE, progressItems.get(0).getDirection());
        assertEquals(ImpactDirection.POSITIVE, progressItems.get(1).getDirection());
    }

    @Test
    void calculateGoalProgress_shouldCalculateCorrectly_withMixedImpactDirections() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(0.7);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator2);
        gi2.setAggregationWeight(0.3);
        gi2.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(2, result.getIndicatorProgress().size());
        
        List<IndicatorProgressItem> progressItems = result.getIndicatorProgress();
        assertEquals(0.7, progressItems.get(0).getWeight());
        assertEquals(0.3, progressItems.get(1).getWeight());
        assertEquals(ImpactDirection.POSITIVE, progressItems.get(0).getDirection());
        assertEquals(ImpactDirection.NEGATIVE, progressItems.get(1).getDirection());
    }

    @Test
    void calculateGoalProgress_shouldCalculateCorrectly_withNeutralImpact() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(0.5);
        gi1.setImpactDirection(ImpactDirection.NEUTRAL);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator2);
        gi2.setAggregationWeight(0.5);
        gi2.setImpactDirection(ImpactDirection.NEUTRAL);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(2, result.getIndicatorProgress().size());
        
        List<IndicatorProgressItem> progressItems = result.getIndicatorProgress();
        assertEquals(0.5, progressItems.get(0).getWeight());
        assertEquals(0.5, progressItems.get(1).getWeight());
        assertEquals(ImpactDirection.NEUTRAL, progressItems.get(0).getDirection());
        assertEquals(ImpactDirection.NEUTRAL, progressItems.get(1).getDirection());
    }

    @Test
    void calculateGoalProgress_shouldReturnZeroProgress_whenNoIndicators() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList());

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals(0.0, result.getOverallProgress());
        assertEquals(0.0, result.getTotalWeight());
        assertEquals("NO_INDICATORS", result.getProgressStatus());
        assertTrue(result.getIndicatorProgress().isEmpty());
    }

    @Test
    void calculateGoalProgress_shouldHandleUnevenWeights() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(0.8);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator2);
        gi2.setAggregationWeight(0.2);
        gi2.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(2, result.getIndicatorProgress().size());
        
        List<IndicatorProgressItem> progressItems = result.getIndicatorProgress();
        assertEquals(0.8, progressItems.get(0).getWeight());
        assertEquals(0.2, progressItems.get(1).getWeight());
    }

    @Test
    void calculateGoalProgress_shouldHandleSingleIndicator() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(1.0);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(1, result.getIndicatorProgress().size());
        
        IndicatorProgressItem progressItem = result.getIndicatorProgress().get(0);
        assertEquals(1.0, progressItem.getWeight());
        assertEquals(ImpactDirection.POSITIVE, progressItem.getDirection());
    }

    @Test
    void calculateGoalProgress_shouldHandleMultipleIndicatorsWithSameWeight() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator1);
        gi1.setAggregationWeight(0.5);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator2);
        gi2.setAggregationWeight(0.5);
        gi2.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1.0, result.getTotalWeight());
        assertEquals(2, result.getIndicatorProgress().size());
        
        List<IndicatorProgressItem> progressItems = result.getIndicatorProgress();
        assertEquals(0.5, progressItems.get(0).getWeight());
        assertEquals(0.5, progressItems.get(1).getWeight());
        assertEquals(ImpactDirection.POSITIVE, progressItems.get(0).getDirection());
        assertEquals(ImpactDirection.NEGATIVE, progressItems.get(1).getDirection());
    }

    @Test
    void calculateGoalProgress_shouldHandleGoalNotFound() {
        // Given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(io.dashboard.exception.ResourceNotFoundException.class, () ->
                goalIndicatorService.calculateGoalProgress(999L));
    }

    @Test
    void bulkLinkIndicators_shouldHandleMultipleValidRequests() {
        // Given
        io.dashboard.dto.GoalIndicatorLinkRequest request1 = new io.dashboard.dto.GoalIndicatorLinkRequest();
        request1.setIndicatorId(1L);
        request1.setAggregationWeight(0.6);
        request1.setImpactDirection(ImpactDirection.POSITIVE);

        io.dashboard.dto.GoalIndicatorLinkRequest request2 = new io.dashboard.dto.GoalIndicatorLinkRequest();
        request2.setIndicatorId(2L);
        request2.setAggregationWeight(0.4);
        request2.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalRepository.existsById(1L)).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator1));
        when(indicatorRepository.findById(2L)).thenReturn(Optional.of(indicator2));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(anyLong(), anyLong())).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenAnswer(invocation -> {
            GoalIndicator gi = invocation.getArgument(0);
            return gi;
        });

        // When
        var result = goalIndicatorService.bulkLinkIndicators(1L, Arrays.asList(request1, request2));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(goalIndicatorRepository, times(2)).save(any(GoalIndicator.class));
    }

    @Test
    void bulkLinkIndicators_shouldFailOnFirstInvalidRequest() {
        // Given
        io.dashboard.dto.GoalIndicatorLinkRequest request1 = new io.dashboard.dto.GoalIndicatorLinkRequest();
        request1.setIndicatorId(1L);
        request1.setAggregationWeight(1.5); // Invalid weight
        request1.setImpactDirection(ImpactDirection.POSITIVE);

        io.dashboard.dto.GoalIndicatorLinkRequest request2 = new io.dashboard.dto.GoalIndicatorLinkRequest();
        request2.setIndicatorId(2L);
        request2.setAggregationWeight(0.5);
        request2.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertThrows(io.dashboard.exception.BadRequestException.class, () ->
                goalIndicatorService.bulkLinkIndicators(1L, Arrays.asList(request1, request2)));
        verify(goalIndicatorRepository, never()).save(any());
    }
} 