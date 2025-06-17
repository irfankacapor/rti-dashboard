package io.dashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.goal.*;
import io.dashboard.entity.Goal;
import io.dashboard.entity.GoalIndicator;
import io.dashboard.entity.GoalTarget;
import io.dashboard.entity.GoalType;
import io.dashboard.model.Indicator;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTargetRepository;
import io.dashboard.repository.GoalTypeRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {
    
    @Mock
    private GoalRepository goalRepository;
    
    @Mock
    private GoalTypeRepository goalTypeRepository;
    
    @Mock
    private GoalTargetRepository goalTargetRepository;
    
    @Mock
    private GoalIndicatorRepository goalIndicatorRepository;
    
    @Mock
    private IndicatorRepository indicatorRepository;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private GoalService goalService;
    
    private GoalType goalType;
    private Goal goal;
    private Indicator indicator;
    private GoalTarget goalTarget;
    private GoalIndicator goalIndicator;
    private GoalCreateRequest createRequest;
    private GoalUpdateRequest updateRequest;
    private GoalTargetRequest targetRequest;
    
    @BeforeEach
    void setUp() {
        goalType = GoalType.builder()
                .id(1L)
                .name("SDG Goals")
                .description("Sustainable Development Goals")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        goal = Goal.builder()
                .id(1L)
                .name("End Poverty")
                .description("End poverty in all its forms everywhere")
                .url("https://sdgs.un.org/goals/goal1")
                .year(2030)
                .goalType(goalType)
                .attributes("{\"sdg_number\": 1, \"category\": \"social\"}")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        indicator = Indicator.builder()
                .id(1L)
                .name("Poverty Rate")
                .description("Percentage of population below poverty line")
                .build();
        
        goalTarget = GoalTarget.builder()
                .id(1L)
                .goal(goal)
                .indicator(indicator)
                .targetYear(2030)
                .targetValue(new BigDecimal("5.0"))
                .targetType(GoalTarget.TargetType.ABSOLUTE)
                .targetPer(new BigDecimal("50.0"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        goalIndicator = GoalIndicator.builder()
                .id(1L)
                .goal(goal)
                .indicator(indicator)
                .aggregationWeight(new BigDecimal("0.5"))
                .impactDirection(GoalIndicator.ImpactDirection.NEGATIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        createRequest = GoalCreateRequest.builder()
                .name("Test Goal")
                .description("Test description")
                .url("https://example.com")
                .year(2030)
                .goalTypeId(1L)
                .attributes("{\"test\": true}")
                .build();
        
        updateRequest = GoalUpdateRequest.builder()
                .name("Updated Goal")
                .description("Updated description")
                .url("https://updated.com")
                .year(2035)
                .goalTypeId(1L)
                .attributes("{\"updated\": true}")
                .build();
        
        targetRequest = GoalTargetRequest.builder()
                .indicatorId(1L)
                .targetYear(2030)
                .targetValue(new BigDecimal("10.0"))
                .targetType(GoalTarget.TargetType.ABSOLUTE)
                .targetPer(new BigDecimal("25.0"))
                .build();
    }
    
    @Test
    void findAll_shouldReturnAllGoals() {
        // Given
        List<Goal> goals = Arrays.asList(goal);
        when(goalRepository.findAll()).thenReturn(goals);
        when(goalTargetRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalTarget));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(1L);
        
        // When
        List<GoalResponse> result = goalService.findAll();
        
        // Then
        assertEquals(1, result.size());
        assertEquals("End Poverty", result.get(0).getName());
        assertEquals(1, result.get(0).getTargets().size());
        assertEquals(1, result.get(0).getIndicators().size());
        
        verify(goalRepository).findAll();
        verify(goalTargetRepository).findByGoalIdWithIndicator(1L);
        verify(goalIndicatorRepository).findByGoalIdWithIndicator(1L);
    }
    
    @Test
    void findById_shouldReturnGoal_whenExists() {
        // Given
        when(goalRepository.findByIdWithTargetsAndIndicators(1L)).thenReturn(Optional.of(goal));
        when(goalTargetRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalTarget));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(1L);
        
        // When
        GoalResponse result = goalService.findById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("End Poverty", result.getName());
        assertEquals("End poverty in all its forms everywhere", result.getDescription());
        assertEquals(1, result.getTargets().size());
        assertEquals(1, result.getIndicators().size());
        
        verify(goalRepository).findByIdWithTargetsAndIndicators(1L);
    }
    
    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Given
        when(goalRepository.findByIdWithTargetsAndIndicators(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.findById(999L));
        assertEquals("Goal not found with ID: 999", exception.getMessage());
        
        verify(goalRepository).findByIdWithTargetsAndIndicators(999L);
    }
    
    @Test
    void findByType_shouldReturnGoals_whenTypeExists() {
        // Given
        when(goalTypeRepository.existsById(1L)).thenReturn(true);
        when(goalRepository.findByGoalTypeId(1L)).thenReturn(Arrays.asList(goal));
        when(goalTargetRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalTarget));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(1L);
        
        // When
        List<GoalResponse> result = goalService.findByType(1L);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("End Poverty", result.get(0).getName());
        
        verify(goalTypeRepository).existsById(1L);
        verify(goalRepository).findByGoalTypeId(1L);
    }
    
    @Test
    void findByType_shouldThrowException_whenTypeNotFound() {
        // Given
        when(goalTypeRepository.existsById(999L)).thenReturn(false);
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.findByType(999L));
        assertEquals("Goal type not found with ID: 999", exception.getMessage());
        
        verify(goalTypeRepository).existsById(999L);
    }
    
    @Test
    void create_shouldCreateGoal_whenValidRequest() throws Exception {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType));
        when(objectMapper.readTree("{\"test\": true}")).thenReturn(null);
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(goalTargetRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalTarget));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(1L);
        
        // When
        GoalResponse result = goalService.create(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("End Poverty", result.getName());
        
        verify(goalTypeRepository).findById(1L);
        verify(objectMapper).readTree("{\"test\": true}");
        verify(goalRepository).save(any(Goal.class));
    }
    
    @Test
    void create_shouldThrowException_whenGoalTypeNotFound() {
        // Given
        when(goalTypeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.create(createRequest.toBuilder().goalTypeId(999L).build()));
        assertEquals("Goal type not found with ID: 999", exception.getMessage());
        
        verify(goalTypeRepository).findById(999L);
        verify(goalRepository, never()).save(any());
    }
    
    @Test
    void create_shouldThrowException_whenInvalidJsonAttributes() throws Exception {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType));
        when(objectMapper.readTree("invalid json")).thenThrow(new RuntimeException("Invalid JSON"));
        
        GoalCreateRequest invalidRequest = createRequest.toBuilder().attributes("invalid json").build();
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalService.create(invalidRequest));
        assertTrue(exception.getMessage().contains("Invalid JSON format in attributes"));
        
        verify(goalTypeRepository).findById(1L);
        verify(objectMapper).readTree("invalid json");
        verify(goalRepository, never()).save(any());
    }
    
    @Test
    void update_shouldUpdateGoal_whenValidRequest() throws Exception {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType));
        when(objectMapper.readTree("{\"updated\": true}")).thenReturn(null);
        when(goalRepository.save(any(Goal.class))).thenReturn(goal);
        when(goalTargetRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalTarget));
        when(goalIndicatorRepository.findByGoalIdWithIndicator(1L)).thenReturn(Arrays.asList(goalIndicator));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(1L);
        
        // When
        GoalResponse result = goalService.update(1L, updateRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Goal", result.getName());
        
        verify(goalRepository).findById(1L);
        verify(goalTypeRepository).findById(1L);
        verify(objectMapper).readTree("{\"updated\": true}");
        verify(goalRepository).save(any(Goal.class));
    }
    
    @Test
    void update_shouldThrowException_whenGoalNotFound() {
        // Given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.update(999L, updateRequest));
        assertEquals("Goal not found with ID: 999", exception.getMessage());
        
        verify(goalRepository).findById(999L);
        verify(goalRepository, never()).save(any());
    }
    
    @Test
    void delete_shouldDeleteGoal_whenNoTargetsOrIndicators() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);
        when(goalRepository.countIndicatorsByGoalId(1L)).thenReturn(0L);
        
        // When
        goalService.delete(1L);
        
        // Then
        verify(goalRepository).findById(1L);
        verify(goalRepository).countTargetsByGoalId(1L);
        verify(goalRepository).countIndicatorsByGoalId(1L);
        verify(goalRepository).delete(goal);
    }
    
    @Test
    void delete_shouldThrowException_whenHasTargets() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(5L);
        when(goalRepository.countIndicatorsByGoalId(1L)).thenReturn(0L);
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalService.delete(1L));
        assertEquals("Cannot delete goal with ID 1 because it has 5 targets and 0 indicator relationships", 
                     exception.getMessage());
        
        verify(goalRepository).findById(1L);
        verify(goalRepository).countTargetsByGoalId(1L);
        verify(goalRepository).countIndicatorsByGoalId(1L);
        verify(goalRepository, never()).delete(any());
    }
    
    @Test
    void addTarget_shouldAddTarget_whenValidRequest() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(indicator));
        when(goalTargetRepository.save(any(GoalTarget.class))).thenReturn(goalTarget);
        
        // When
        GoalTargetResponse result = goalService.addTarget(1L, targetRequest);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Poverty Rate", result.getIndicatorName());
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalTargetRepository).save(any(GoalTarget.class));
    }
    
    @Test
    void addTarget_shouldThrowException_whenGoalNotFound() {
        // Given
        when(goalRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.addTarget(999L, targetRequest));
        assertEquals("Goal not found with ID: 999", exception.getMessage());
        
        verify(goalRepository).findById(999L);
        verify(goalTargetRepository, never()).save(any());
    }
    
    @Test
    void addTarget_shouldThrowException_whenIndicatorNotFound() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(indicatorRepository.findById(999L)).thenReturn(Optional.empty());
        
        GoalTargetRequest invalidRequest = targetRequest.toBuilder().indicatorId(999L).build();
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.addTarget(1L, invalidRequest));
        assertEquals("Indicator not found with ID: 999", exception.getMessage());
        
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(999L);
        verify(goalTargetRepository, never()).save(any());
    }
    
    @Test
    void addTarget_shouldThrowException_whenInvalidTargetYear() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        
        GoalTargetRequest invalidRequest = targetRequest.toBuilder().targetYear(1800).build();
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalService.addTarget(1L, invalidRequest));
        assertEquals("Target year must be between 1900 and 2100", exception.getMessage());
        
        verify(goalRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }
    
    @Test
    void addTarget_shouldThrowException_whenInvalidTargetValue() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        
        GoalTargetRequest invalidRequest = targetRequest.toBuilder().targetValue(BigDecimal.ZERO).build();
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalService.addTarget(1L, invalidRequest));
        assertEquals("Target value must be positive", exception.getMessage());
        
        verify(goalRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }
    
    @Test
    void removeTarget_shouldRemoveTarget_whenExists() {
        // Given
        when(goalTargetRepository.findByGoalIdAndId(1L, 1L)).thenReturn(Optional.of(goalTarget));
        
        // When
        goalService.removeTarget(1L, 1L);
        
        // Then
        verify(goalTargetRepository).findByGoalIdAndId(1L, 1L);
        verify(goalTargetRepository).delete(goalTarget);
    }
    
    @Test
    void removeTarget_shouldThrowException_whenNotFound() {
        // Given
        when(goalTargetRepository.findByGoalIdAndId(1L, 999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalService.removeTarget(1L, 999L));
        assertEquals("Target not found with ID: 999 for goal with ID: 1", exception.getMessage());
        
        verify(goalTargetRepository).findByGoalIdAndId(1L, 999L);
        verify(goalTargetRepository, never()).delete(any());
    }
} 