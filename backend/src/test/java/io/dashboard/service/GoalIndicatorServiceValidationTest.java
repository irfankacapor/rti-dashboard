package io.dashboard.service;

import io.dashboard.dto.GoalIndicatorLinkRequest;
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

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalIndicatorServiceValidationTest {

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
        indicator.setUnitPrefix("€");
        indicator.setUnitSuffix("thousand");
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenWeightIsNegative() {
        // No stubbing needed, weight validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, -0.1, ImpactDirection.POSITIVE));
        assertTrue(exception.getMessage().contains("Aggregation weight must be between 0.0 and 1.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenWeightIsGreaterThanOne() {
        // No stubbing needed, weight validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 1.5, ImpactDirection.POSITIVE));
        assertTrue(exception.getMessage().contains("Aggregation weight must be between 0.0 and 1.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenWeightIsZero() {
        // No stubbing needed, weight validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 0.0, ImpactDirection.POSITIVE));
        assertTrue(exception.getMessage().contains("Aggregation weight must be greater than 0.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldSucceed_whenWeightIsOne() {
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenAnswer(invocation -> {
            GoalIndicator gi = invocation.getArgument(0);
            return gi;
        });
        assertDoesNotThrow(() ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 1.0, ImpactDirection.POSITIVE));
        verify(goalIndicatorRepository).save(any(GoalIndicator.class));
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenDuplicateRelationship() {
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 0.5, ImpactDirection.POSITIVE));
        assertTrue(exception.getMessage().contains("Goal is already linked to this indicator"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenInvalidGoalReference() {
        when(goalRepository.existsById(999L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(999L, 1L, 0.5, ImpactDirection.POSITIVE));
        assertEquals("Goal not found with id : '999'", exception.getMessage());
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldFail_whenInvalidIndicatorReference() {
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(999L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 999L, 0.5, ImpactDirection.POSITIVE));
        assertEquals("Indicator not found with id : '999'", exception.getMessage());
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void linkGoalToIndicator_shouldSucceed_withAllImpactDirections() {
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(anyLong())).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(anyLong())).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(anyLong(), anyLong())).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenAnswer(invocation -> {
            GoalIndicator gi = invocation.getArgument(0);
            return gi;
        });
        assertDoesNotThrow(() ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, 0.5, ImpactDirection.POSITIVE));
        assertDoesNotThrow(() ->
                goalIndicatorService.linkGoalToIndicator(1L, 2L, 0.5, ImpactDirection.NEGATIVE));
        assertDoesNotThrow(() ->
                goalIndicatorService.linkGoalToIndicator(1L, 3L, 0.5, ImpactDirection.NEUTRAL));
    }

    @Test
    void updateGoalIndicatorWeight_shouldFail_whenWeightIsNegative() {
        // No stubbing needed, weight validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.updateGoalIndicatorWeight(1L, 1L, -0.1));
        assertTrue(exception.getMessage().contains("Aggregation weight must be between 0.0 and 1.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void updateGoalIndicatorWeight_shouldFail_whenWeightIsGreaterThanOne() {
        // No stubbing needed, weight validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.updateGoalIndicatorWeight(1L, 1L, 1.5));
        assertTrue(exception.getMessage().contains("Aggregation weight must be between 0.0 and 1.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void updateGoalIndicatorWeight_shouldFail_whenRelationshipNotFound() {
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.updateGoalIndicatorWeight(1L, 1L, 0.7));
        assertEquals("GoalIndicator not found with goalId and indicatorId : '1 and 1'", exception.getMessage());
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void updateGoalIndicatorDirection_shouldFail_whenRelationshipNotFound() {
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.updateGoalIndicatorDirection(1L, 1L, ImpactDirection.NEGATIVE));
        assertEquals("GoalIndicator not found with goalId and indicatorId : '1 and 1'", exception.getMessage());
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void bulkLinkIndicators_shouldFail_whenEmptyList() {
        // No stubbing needed, empty list validation happens first
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.bulkLinkIndicators(1L, Arrays.asList()));
        assertTrue(exception.getMessage().contains("Links list cannot be empty"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void bulkLinkIndicators_shouldFail_whenGoalNotFound() {
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(0.5);
        request.setImpactDirection(ImpactDirection.POSITIVE);
        when(goalRepository.existsById(999L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.bulkLinkIndicators(999L, Arrays.asList(request)));
        assertEquals("Goal not found with id : '999'", exception.getMessage());
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void bulkLinkIndicators_shouldFail_whenInvalidWeightInRequest() {
        GoalIndicatorLinkRequest request = new GoalIndicatorLinkRequest();
        request.setIndicatorId(1L);
        request.setAggregationWeight(1.5); // Invalid weight
        request.setImpactDirection(ImpactDirection.POSITIVE);
        when(goalRepository.existsById(1L)).thenReturn(true);
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                goalIndicatorService.bulkLinkIndicators(1L, Arrays.asList(request)));
        assertTrue(exception.getMessage().contains("Aggregation weight must be between 0.0 and 1.0"));
        verify(goalIndicatorRepository, never()).save(any());
    }

    @Test
    void validateGoalIndicatorLink_shouldFail_whenGoalNotFound() {
        when(goalRepository.existsById(999L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.validateGoalIndicatorLink(999L, 1L));
        assertEquals("Goal not found with id : '999'", exception.getMessage());
    }

    @Test
    void validateGoalIndicatorLink_shouldFail_whenIndicatorNotFound() {
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(999L)).thenReturn(false);
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.validateGoalIndicatorLink(1L, 999L));
        assertEquals("Indicator not found with id : '999'", exception.getMessage());
    }
} 