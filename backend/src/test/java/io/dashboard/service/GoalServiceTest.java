package io.dashboard.service;

import io.dashboard.dto.GoalResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalType;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private GoalService goalService;

    private GoalType testGoalType;
    private Goal testGoal;
    private GoalResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testGoalType = GoalType.builder()
                .id(1L)
                .name("SDGs")
                .description("Sustainable Development Goals")
                .createdAt(LocalDateTime.now())
                .build();

        testGoal = Goal.builder()
                .id(1L)
                .goalType(testGoalType)
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes("{\"key\": \"value\"}")
                .createdAt(LocalDateTime.now())
                .build();

        expectedResponse = GoalResponse.builder()
                .id(1L)
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes("{\"key\": \"value\"}")
                .createdAt(testGoal.getCreatedAt())
                .targetCount(0L)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllGoals() {
        // Given
        List<Goal> goals = Arrays.asList(testGoal);
        when(goalRepository.findAll()).thenReturn(goals);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        List<GoalResponse> result = goalService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getName(), result.get(0).getName());
        verify(goalRepository).findAll();
    }

    @Test
    void findByGoalTypeId_ShouldReturnGoalsForType() {
        // Given
        List<Goal> goals = Arrays.asList(testGoal);
        when(goalRepository.findByGoalTypeIdWithTargets(1L)).thenReturn(goals);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        List<GoalResponse> result = goalService.findByGoalTypeId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getName(), result.get(0).getName());
        verify(goalRepository).findByGoalTypeIdWithTargets(1L);
    }

    @Test
    void findById_ShouldReturnGoal_WhenExists() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        GoalResponse result = goalService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.findById(1L));
        verify(goalRepository).findById(1L);
    }

    @Test
    void create_ShouldCreateGoal_WhenValidData() {
        // Given
        Goal goalToCreate = Goal.builder()
                .goalType(GoalType.builder().id(1L).build())
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes("{\"key\": \"value\"}")
                .build();

        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(testGoalType));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        GoalResponse result = goalService.create(goalToCreate);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalTypeRepository).findById(1L);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void create_ShouldThrowException_WhenGoalTypeNotFound() {
        // Given
        Goal goalToCreate = Goal.builder()
                .goalType(GoalType.builder().id(1L).build())
                .name("Goal 1")
                .build();

        when(goalTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.create(goalToCreate));
        verify(goalTypeRepository).findById(1L);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateGoal_WhenValidData() {
        // Given
        Goal goalToUpdate = Goal.builder()
                .goalType(GoalType.builder().id(2L).build())
                .name("Updated Goal")
                .url("https://updated.com")
                .year(2026)
                .description("Updated description")
                .attributes("{\"updated\": \"value\"}")
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalTypeRepository.findById(2L)).thenReturn(Optional.of(testGoalType));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        GoalResponse result = goalService.update(1L, goalToUpdate);

        // Then
        assertNotNull(result);
        verify(goalRepository).findById(1L);
        verify(goalTypeRepository).findById(2L);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void update_ShouldThrowException_WhenGoalNotFound() {
        // Given
        Goal goalToUpdate = Goal.builder()
                .goalType(GoalType.builder().id(1L).build())
                .name("Updated Goal")
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.update(1L, goalToUpdate));
        verify(goalRepository).findById(1L);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteGoal_WhenNoTargets() {
        // Given
        when(goalRepository.findByIdWithTargets(1L)).thenReturn(testGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        goalService.delete(1L);

        // Then
        verify(goalRepository).findByIdWithTargets(1L);
        verify(goalRepository).countTargetsByGoalId(1L);
        verify(goalRepository).delete(testGoal);
    }

    @Test
    void delete_ShouldThrowException_WhenHasTargets() {
        // Given
        when(goalRepository.findByIdWithTargets(1L)).thenReturn(testGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(5L);

        // When & Then
        assertThrows(BadRequestException.class, () -> goalService.delete(1L));
        verify(goalRepository).findByIdWithTargets(1L);
        verify(goalRepository).countTargetsByGoalId(1L);
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowException_WhenGoalNotFound() {
        // Given
        when(goalRepository.findByIdWithTargets(1L)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.delete(1L));
        verify(goalRepository).findByIdWithTargets(1L);
        verify(goalRepository, never()).delete(any());
    }

    @Test
    void findGoalsWithTargets_ShouldReturnGoal_WhenExists() {
        // Given
        when(goalRepository.findByIdWithTargets(1L)).thenReturn(testGoal);

        // When
        Goal result = goalService.findGoalsWithTargets(1L);

        // Then
        assertNotNull(result);
        assertEquals(testGoal.getName(), result.getName());
        verify(goalRepository).findByIdWithTargets(1L);
    }

    @Test
    void findGoalsWithTargets_ShouldThrowException_WhenNotFound() {
        // Given
        when(goalRepository.findByIdWithTargets(1L)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.findGoalsWithTargets(1L));
        verify(goalRepository).findByIdWithTargets(1L);
    }
} 