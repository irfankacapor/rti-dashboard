package io.dashboard.service;

import io.dashboard.dto.GoalIndicatorLinkRequest;
import io.dashboard.dto.GoalProgressResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalIndicatorServiceTest {

    @Mock
    private GoalIndicatorRepository goalIndicatorRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private GoalIndicatorService goalIndicatorService;

    private Goal goal;
    private Indicator indicator;
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

        indicator = new Indicator();
        indicator.setId(1L);
        indicator.setName("Test Indicator");
        indicator.setCode("TEST_IND");
        indicator.setUnit(unit);
    }

    @Test
    void linkGoalToIndicator_shouldSucceed() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenAnswer(invocation -> {
            GoalIndicator gi = invocation.getArgument(0);
            gi.setCreatedAt(LocalDateTime.now());
            return gi;
        });

        // When
        var result = goalIndicatorService.linkGoalToIndicator(1L, 1L, 0.5, ImpactDirection.POSITIVE);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals(1L, result.getIndicatorId());
        assertEquals(0.5, result.getAggregationWeight());
        assertEquals(ImpactDirection.POSITIVE, result.getImpactDirection());
        verify(goalIndicatorRepository).save(any(GoalIndicator.class));
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(999L, 1L, 0.5, ImpactDirection.POSITIVE));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenIndicatorNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 999L, 0.5, ImpactDirection.POSITIVE));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenAlreadyLinked() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 0.5, ImpactDirection.POSITIVE));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void unlinkGoalFromIndicator_shouldSucceed() {
        // Given
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true);

        // When
        goalIndicatorService.unlinkGoalFromIndicator(1L, 1L);

        // Then
        verify(goalIndicatorRepository).deleteById(any(GoalIndicator.GoalIndicatorId.class));
    }

    @Test
    void unlinkGoalFromIndicator_shouldFail_whenNotLinked() {
        // Given
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.unlinkGoalFromIndicator(1L, 1L));
        verify(goalIndicatorRepository, never()).deleteById(any());
    }

    @Test
    void updateGoalIndicatorWeight_shouldSucceed() {
        // Given
        GoalIndicator existing = new GoalIndicator();
        existing.setGoal(goal);
        existing.setIndicator(indicator);
        existing.setAggregationWeight(0.3);
        existing.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(existing);

        // When
        var result = goalIndicatorService.updateGoalIndicatorWeight(1L, 1L, 0.7);

        // Then
        assertNotNull(result);
        assertEquals(0.7, result.getAggregationWeight());
        verify(goalIndicatorRepository).save(existing);
    }

    @Test
    void updateGoalIndicatorDirection_shouldSucceed() {
        // Given
        GoalIndicator existing = new GoalIndicator();
        existing.setGoal(goal);
        existing.setIndicator(indicator);
        existing.setAggregationWeight(0.5);
        existing.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L))
                .thenReturn(Optional.of(existing));
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(existing);

        // When
        var result = goalIndicatorService.updateGoalIndicatorDirection(1L, 1L, ImpactDirection.NEGATIVE);

        // Then
        assertNotNull(result);
        assertEquals(ImpactDirection.NEGATIVE, result.getImpactDirection());
        verify(goalIndicatorRepository).save(existing);
    }

    @Test
    void findIndicatorsByGoal_shouldReturnList() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator);
        gi1.setAggregationWeight(0.5);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1));

        // When
        var result = goalIndicatorService.findIndicatorsByGoal(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getIndicatorId());
    }

    @Test
    void findIndicatorsByGoal_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.findIndicatorsByGoal(1L));
    }

    @Test
    void findGoalsByIndicator_shouldReturnList() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator);
        gi1.setAggregationWeight(0.5);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.findByIndicatorId(1L)).thenReturn(Arrays.asList(gi1));

        // When
        var result = goalIndicatorService.findGoalsByIndicator(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getIndicatorId());
    }

    @Test
    void calculateGoalProgress_shouldReturnProgress_whenIndicatorsExist() {
        // Given
        GoalIndicator gi1 = new GoalIndicator();
        gi1.setGoal(goal);
        gi1.setIndicator(indicator);
        gi1.setAggregationWeight(0.6);
        gi1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicator gi2 = new GoalIndicator();
        gi2.setGoal(goal);
        gi2.setIndicator(indicator);
        gi2.setAggregationWeight(0.4);
        gi2.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gi1, gi2));

        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals(1.0, result.getTotalWeight());
        assertNotNull(result.getIndicatorProgress());
        assertEquals(2, result.getIndicatorProgress().size());
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
    }

    @Test
    void bulkLinkIndicators_shouldSucceed() {
        // Given
        GoalIndicatorLinkRequest request1 = new GoalIndicatorLinkRequest();
        request1.setIndicatorId(1L);
        request1.setAggregationWeight(0.6);
        request1.setImpactDirection(ImpactDirection.POSITIVE);

        GoalIndicatorLinkRequest request2 = new GoalIndicatorLinkRequest();
        request2.setIndicatorId(2L);
        request2.setAggregationWeight(0.4);
        request2.setImpactDirection(ImpactDirection.NEGATIVE);

        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.findById(2L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(anyLong(), anyLong())).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenAnswer(invocation -> {
            GoalIndicator gi = invocation.getArgument(0);
            gi.setCreatedAt(LocalDateTime.now());
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
    void bulkLinkIndicators_shouldFail_whenGoalNotFound() {
        // Given
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(0.5);
        request.setImpactDirection(ImpactDirection.POSITIVE);

        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.bulkLinkIndicators(1L, Arrays.asList(request)));
    }

    @Test
    void validateGoalIndicatorLink_shouldSucceed_whenBothExist() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> goalIndicatorService.validateGoalIndicatorLink(1L, 1L));
    }

    @Test
    void validateGoalIndicatorLink_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.validateGoalIndicatorLink(1L, 1L));
    }

    @Test
    void validateGoalIndicatorLink_shouldFail_whenIndicatorNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.validateGoalIndicatorLink(1L, 1L));
    }
} 