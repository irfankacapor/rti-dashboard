package io.dashboard.service;

import io.dashboard.dto.GoalTypeResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.GoalType;
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
class GoalTypeServiceTest {

    @Mock
    private GoalTypeRepository goalTypeRepository;

    @InjectMocks
    private GoalTypeService goalTypeService;

    private GoalType testGoalType;
    private GoalTypeResponse expectedResponse;

    @BeforeEach
    void setUp() {
        testGoalType = GoalType.builder()
                .id(1L)
                .name("SDGs")
                .description("Sustainable Development Goals")
                .createdAt(LocalDateTime.now())
                .build();

        expectedResponse = GoalTypeResponse.builder()
                .id(1L)
                .name("SDGs")
                .description("Sustainable Development Goals")
                .createdAt(testGoalType.getCreatedAt())
                .goalCount(0L)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllGoalTypes() {
        // Given
        List<GoalType> goalTypes = Arrays.asList(testGoalType);
        when(goalTypeRepository.findAllWithGoals()).thenReturn(goalTypes);
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(0L);

        // When
        List<GoalTypeResponse> result = goalTypeService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getName(), result.get(0).getName());
        verify(goalTypeRepository).findAllWithGoals();
    }

    @Test
    void findById_ShouldReturnGoalType_WhenExists() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(testGoalType));
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(0L);

        // When
        GoalTypeResponse result = goalTypeService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalTypeRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTypeService.findById(1L));
        verify(goalTypeRepository).findById(1L);
    }

    @Test
    void create_ShouldCreateGoalType_WhenValidData() {
        // Given
        when(goalTypeRepository.existsByName("SDGs")).thenReturn(false);
        when(goalTypeRepository.save(any(GoalType.class))).thenReturn(testGoalType);
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(0L);

        // When
        GoalTypeResponse result = goalTypeService.create(testGoalType);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalTypeRepository).existsByName("SDGs");
        verify(goalTypeRepository).save(testGoalType);
    }

    @Test
    void create_ShouldThrowException_WhenNameExists() {
        // Given
        when(goalTypeRepository.existsByName("SDGs")).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTypeService.create(testGoalType));
        verify(goalTypeRepository).existsByName("SDGs");
        verify(goalTypeRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateGoalType_WhenValidData() {
        // Given
        GoalType existingGoalType = GoalType.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Description")
                .build();

        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(existingGoalType));
        when(goalTypeRepository.existsByName("SDGs")).thenReturn(false);
        when(goalTypeRepository.save(any(GoalType.class))).thenReturn(testGoalType);
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(0L);

        // When
        GoalTypeResponse result = goalTypeService.update(1L, testGoalType);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).existsByName("SDGs");
        verify(goalTypeRepository).save(any(GoalType.class));
    }

    @Test
    void update_ShouldThrowException_WhenNotFound() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTypeService.update(1L, testGoalType));
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenNewNameExists() {
        // Given
        GoalType existingGoalType = GoalType.builder()
                .id(1L)
                .name("Old Name")
                .description("Old Description")
                .build();

        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(existingGoalType));
        when(goalTypeRepository.existsByName("SDGs")).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTypeService.update(1L, testGoalType));
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).existsByName("SDGs");
        verify(goalTypeRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteGoalType_WhenNoGoals() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(testGoalType));
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(0L);

        // When
        goalTypeService.delete(1L);

        // Then
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).countGoalsByGoalTypeId(1L);
        verify(goalTypeRepository).delete(testGoalType);
    }

    @Test
    void delete_ShouldThrowException_WhenHasGoals() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(testGoalType));
        when(goalTypeRepository.countGoalsByGoalTypeId(1L)).thenReturn(5L);

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTypeService.delete(1L));
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).countGoalsByGoalTypeId(1L);
        verify(goalTypeRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowException_WhenNotFound() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTypeService.delete(1L));
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository, never()).delete(any());
    }
} 