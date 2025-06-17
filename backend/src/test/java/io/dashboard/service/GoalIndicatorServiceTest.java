package io.dashboard.service;

import io.dashboard.dto.goal.*;
import io.dashboard.entity.Goal;
import io.dashboard.entity.GoalIndicator;
import io.dashboard.entity.GoalTarget;
import io.dashboard.enums.ImpactDirection;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Indicator;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTargetRepository;
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
import java.util.List;
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
    
    @Mock
    private GoalTargetRepository goalTargetRepository;
    
    @InjectMocks
    private GoalIndicatorService goalIndicatorService;
    
    private Goal goal;
    private Indicator indicator;
    private GoalIndicator goalIndicator;
    private GoalTarget goalTarget;
    
    @BeforeEach
    void setUp() {
        goal = Goal.builder()
                .id(1L)
                .name("Test Goal")
                .description("Test Goal Description")
                .build();
        
        indicator = Indicator.builder()
                .id(1L)
                .name("Test Indicator")
                .build();
        
        goalIndicator = GoalIndicator.builder()
                .id(1L)
                .goal(goal)
                .indicator(indicator)
                .aggregationWeight(new BigDecimal("0.5"))
                .impactDirection(ImpactDirection.POSITIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        goalTarget = GoalTarget.builder()
                .id(1L)
                .goal(goal)
                .indicator(indicator)
                .targetValue(new BigDecimal("100.0"))
                .targetYear(2025)
                .build();
    }
    
    @Test
    void linkGoalToIndicator_shouldCreateRelationship_whenValidRequest() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        when(goalIndicatorRepository.sumWeightsByGoalId(1L)).thenReturn(BigDecimal.ZERO);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(goalIndicator);
        
        // When
        GoalIndicatorResponse result = goalIndicatorService.linkGoalToIndicator(
                1L, 1L, new BigDecimal("0.5"), ImpactDirection.POSITIVE);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals(1L, result.getIndicatorId());
        assertEquals(new BigDecimal("0.5"), result.getAggregationWeight());
        assertEquals(ImpactDirection.POSITIVE, result.getImpactDirection());
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalIndicatorRepository).existsByGoalIdAndIndicatorId(1L, 1L);
        verify(goalIndicatorRepository).save(any(GoalIndicator.class));
    }
    
    @Test
    void linkGoalToIndicator_shouldThrowException_whenGoalNotFound() {
        // Given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(999L, 1L, new BigDecimal("0.5"), ImpactDirection.POSITIVE));
        
        verify(goalRepository).findById(999L);
        verifyNoInteractions(indicatorRepository, goalIndicatorRepository);
    }
    
    @Test
    void linkGoalToIndicator_shouldThrowException_whenIndicatorNotFound() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 999L, new BigDecimal("0.5"), ImpactDirection.POSITIVE));
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(999L);
        verifyNoInteractions(goalIndicatorRepository);
    }
    
    @Test
    void linkGoalToIndicator_shouldThrowException_whenRelationshipExists() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true);
        
        // When & Then
        assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, new BigDecimal("0.5"), ImpactDirection.POSITIVE));
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalIndicatorRepository).existsByGoalIdAndIndicatorId(1L, 1L);
        verifyNoMoreInteractions(goalIndicatorRepository);
    }
    
    @Test
    void linkGoalToIndicator_shouldThrowException_whenInvalidWeight() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        
        // When & Then
        assertThrows(BadRequestException.class, () ->
                goalIndicatorService.linkGoalToIndicator(1L, 1L, new BigDecimal("1.5"), ImpactDirection.POSITIVE));
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalIndicatorRepository).existsByGoalIdAndIndicatorId(1L, 1L);
        verifyNoMoreInteractions(goalIndicatorRepository);
    }
    
    @Test
    void unlinkGoalFromIndicator_shouldDeleteRelationship_whenExists() {
        // Given
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.of(goalIndicator));
        
        // When
        goalIndicatorService.unlinkGoalFromIndicator(1L, 1L);
        
        // Then
        verify(goalIndicatorRepository).findByGoalIdAndIndicatorId(1L, 1L);
        verify(goalIndicatorRepository).delete(goalIndicator);
    }
    
    @Test
    void unlinkGoalFromIndicator_shouldThrowException_whenRelationshipNotFound() {
        // Given
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.unlinkGoalFromIndicator(1L, 1L));
        
        verify(goalIndicatorRepository).findByGoalIdAndIndicatorId(1L, 1L);
        verifyNoMoreInteractions(goalIndicatorRepository);
    }
    
    @Test
    void updateGoalIndicatorWeight_shouldUpdateWeight_whenValidRequest() {
        // Given
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.of(goalIndicator));
        when(goalIndicatorRepository.sumWeightsByGoalId(1L)).thenReturn(new BigDecimal("0.5"));
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(goalIndicator);
        
        // When
        GoalIndicatorResponse result = goalIndicatorService.updateGoalIndicatorWeight(1L, 1L, new BigDecimal("0.7"));
        
        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("0.7"), result.getAggregationWeight());
        
        verify(goalIndicatorRepository).findByGoalIdAndIndicatorId(1L, 1L);
        verify(goalIndicatorRepository).sumWeightsByGoalId(1L);
        verify(goalIndicatorRepository).save(any(GoalIndicator.class));
    }
    
    @Test
    void updateImpactDirection_shouldUpdateImpactDirection_whenValidRequest() {
        // Given
        when(goalIndicatorRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Optional.of(goalIndicator));
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(goalIndicator);
        
        // When
        GoalIndicatorResponse result = goalIndicatorService.updateImpactDirection(1L, 1L, ImpactDirection.NEGATIVE);
        
        // Then
        assertNotNull(result);
        assertEquals(ImpactDirection.NEGATIVE, result.getImpactDirection());
        
        verify(goalIndicatorRepository).findByGoalIdAndIndicatorId(1L, 1L);
        verify(goalIndicatorRepository).save(any(GoalIndicator.class));
    }
    
    @Test
    void findIndicatorsByGoal_shouldReturnIndicators_whenGoalExists() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        
        // When
        List<GoalIndicatorResponse> result = goalIndicatorService.findIndicatorsByGoal(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getIndicatorId());
        
        verify(goalRepository).existsById(1L);
        verify(goalIndicatorRepository).findByGoalIdWithIndicator(1L);
    }
    
    @Test
    void findIndicatorsByGoal_shouldThrowException_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(999L)).thenReturn(false);
        
        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalIndicatorService.findIndicatorsByGoal(999L));
        
        verify(goalRepository).existsById(999L);
        verifyNoInteractions(goalIndicatorRepository);
    }
    
    @Test
    void findGoalsByIndicator_shouldReturnGoals_whenIndicatorExists() {
        // Given
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(goalIndicatorRepository.findByIndicatorIdWithGoal(1L)).thenReturn(Arrays.asList(goalIndicator));
        
        // When
        List<GoalIndicatorResponse> result = goalIndicatorService.findGoalsByIndicator(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getIndicatorId());
        
        verify(indicatorRepository).existsById(1L);
        verify(goalIndicatorRepository).findByIndicatorIdWithGoal(1L);
    }
    
    @Test
    void validateGoalIndicatorLink_shouldReturnTrue_whenRelationshipExists() {
        // Given
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true);
        
        // When
        boolean result = goalIndicatorService.validateGoalIndicatorLink(1L, 1L);
        
        // Then
        assertTrue(result);
        verify(goalIndicatorRepository).existsByGoalIdAndIndicatorId(1L, 1L);
    }
    
    @Test
    void validateGoalIndicatorLink_shouldReturnFalse_whenRelationshipNotExists() {
        // Given
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        
        // When
        boolean result = goalIndicatorService.validateGoalIndicatorLink(1L, 1L);
        
        // Then
        assertFalse(result);
        verify(goalIndicatorRepository).existsByGoalIdAndIndicatorId(1L, 1L);
    }
    
    @Test
    void calculateGoalProgress_shouldReturnProgress_whenGoalHasIndicators() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTargetRepository.findByGoalIdAndIndicatorId(1L, 1L)).thenReturn(Arrays.asList(goalTarget));
        
        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals(1, result.getTotalIndicators());
        assertEquals(1, result.getIndicatorsWithTargets());
        
        verify(goalRepository).findById(1L);
        verify(goalIndicatorRepository).findByGoalIdWithIndicator(1L);
        verify(goalTargetRepository).findByGoalIdAndIndicatorId(1L, 1L);
    }
    
    @Test
    void calculateGoalProgress_shouldReturnZeroProgress_whenGoalHasNoIndicators() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList());
        
        // When
        GoalProgressResponse result = goalIndicatorService.calculateGoalProgress(1L);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals(0, result.getTotalIndicators());
        assertEquals(0, result.getIndicatorsWithTargets());
        assertEquals(BigDecimal.ZERO, result.getOverallProgress());
        
        verify(goalRepository).findById(1L);
        verify(goalIndicatorRepository).findByGoalIdWithIndicator(1L);
        verifyNoInteractions(goalTargetRepository);
    }
    
    @Test
    void bulkLinkIndicators_shouldCreateMultipleRelationships_whenValidRequest() {
        // Given
        GoalIndicatorLinkRequest request1 = GoalIndicatorLinkRequest.builder()
                .indicatorId(1L)
                .weight(new BigDecimal("0.5"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        
        GoalIndicatorLinkRequest request2 = GoalIndicatorLinkRequest.builder()
                .indicatorId(2L)
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        List<GoalIndicatorLinkRequest> requests = Arrays.asList(request1, request2);
        
        Indicator indicator2 = Indicator.builder().id(2L).name("Test Indicator 2").build();
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(indicatorRepository.findById(2L)).thenReturn(Optional.of(indicator2));
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 2L)).thenReturn(false);
        when(goalIndicatorRepository.save(any(GoalIndicator.class))).thenReturn(goalIndicator);
        
        // When
        List<GoalIndicatorResponse> result = goalIndicatorService.bulkLinkIndicators(1L, requests);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).existsById(1L);
        verify(indicatorRepository).existsById(2L);
        verify(indicatorRepository).findById(1L);
        verify(indicatorRepository).findById(2L);
        verify(goalIndicatorRepository, times(2)).save(any(GoalIndicator.class));
    }
    
    @Test
    void bulkLinkIndicators_shouldThrowException_whenDuplicateIndicatorIds() {
        // Given
        GoalIndicatorLinkRequest request1 = GoalIndicatorLinkRequest.builder()
                .indicatorId(1L)
                .weight(new BigDecimal("0.5"))
                .impactDirection(ImpactDirection.POSITIVE)
                .build();
        
        GoalIndicatorLinkRequest request2 = GoalIndicatorLinkRequest.builder()
                .indicatorId(1L)
                .weight(new BigDecimal("0.3"))
                .impactDirection(ImpactDirection.NEGATIVE)
                .build();
        
        List<GoalIndicatorLinkRequest> requests = Arrays.asList(request1, request2);
        
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        
        // When & Then
        assertThrows(BadRequestException.class, () ->
                goalIndicatorService.bulkLinkIndicators(1L, requests));
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).existsById(1L);
        verifyNoMoreInteractions(indicatorRepository, goalIndicatorRepository);
    }
} 