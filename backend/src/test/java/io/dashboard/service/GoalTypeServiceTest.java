package io.dashboard.service;

import io.dashboard.dto.goal.GoalTypeCreateRequest;
import io.dashboard.dto.goal.GoalTypeResponse;
import io.dashboard.dto.goal.GoalTypeUpdateRequest;
import io.dashboard.model.GoalType;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
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
    
    private GoalType goalType1;
    private GoalType goalType2;
    private GoalTypeCreateRequest createRequest;
    private GoalTypeUpdateRequest updateRequest;
    
    @BeforeEach
    void setUp() {
        goalType1 = GoalType.builder()
                .id(1L)
                .name("SDG Goals")
                .description("Sustainable Development Goals")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        goalType2 = GoalType.builder()
                .id(2L)
                .name("Local Policy")
                .description("Local government policy goals")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        createRequest = GoalTypeCreateRequest.builder()
                .name("Test Goal Type")
                .description("Test description")
                .build();
        
        updateRequest = GoalTypeUpdateRequest.builder()
                .name("Updated Goal Type")
                .description("Updated description")
                .build();
    }
    
    @Test
    void findAll_shouldReturnAllGoalTypes() {
        // Given
        List<GoalType> goalTypes = Arrays.asList(goalType1, goalType2);
        when(goalTypeRepository.findAll()).thenReturn(goalTypes);
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(5L);
        when(goalTypeRepository.countGoalsByTypeId(2L)).thenReturn(3L);
        
        // When
        List<GoalTypeResponse> result = goalTypeService.findAll();
        
        // Then
        assertEquals(2, result.size());
        assertEquals("SDG Goals", result.get(0).getName());
        assertEquals("Local Policy", result.get(1).getName());
        assertEquals(5L, result.get(0).getGoalCount());
        assertEquals(3L, result.get(1).getGoalCount());
        
        verify(goalTypeRepository).findAll();
        verify(goalTypeRepository).countGoalsByTypeId(1L);
        verify(goalTypeRepository).countGoalsByTypeId(2L);
    }
    
    @Test
    void findById_shouldReturnGoalType_whenExists() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType1));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(5L);
        
        // When
        GoalTypeResponse result = goalTypeService.findById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("SDG Goals", result.getName());
        assertEquals("Sustainable Development Goals", result.getDescription());
        assertEquals(5L, result.getGoalCount());
        
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).countGoalsByTypeId(1L);
    }
    
    @Test
    void findById_shouldThrowException_whenNotFound() {
        // Given
        when(goalTypeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalTypeService.findById(999L));
        assertEquals("Goal type not found with ID: 999", exception.getMessage());
        
        verify(goalTypeRepository).findById(999L);
    }
    
    @Test
    void create_shouldCreateGoalType_whenValidRequest() {
        // Given
        when(goalTypeRepository.existsByName("Test Goal Type")).thenReturn(false);
        when(goalTypeRepository.save(any(GoalType.class))).thenReturn(goalType1);
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(0L);
        
        // When
        GoalTypeResponse result = goalTypeService.create(createRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("SDG Goals", result.getName());
        assertEquals("Sustainable Development Goals", result.getDescription());
        
        verify(goalTypeRepository).existsByName("Test Goal Type");
        verify(goalTypeRepository).save(any(GoalType.class));
    }
    
    @Test
    void create_shouldThrowException_whenNameExists() {
        // Given
        when(goalTypeRepository.existsByName("Test Goal Type")).thenReturn(true);
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalTypeService.create(createRequest));
        assertEquals("Goal type with name 'Test Goal Type' already exists", exception.getMessage());
        
        verify(goalTypeRepository).existsByName("Test Goal Type");
        verify(goalTypeRepository, never()).save(any());
    }
    
    @Test
    void update_shouldUpdateGoalType_whenValidRequest() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType1));
        when(goalTypeRepository.existsByName("Updated Goal Type")).thenReturn(false);
        when(goalTypeRepository.save(any(GoalType.class))).thenReturn(goalType1);
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(5L);
        
        // When
        GoalTypeResponse result = goalTypeService.update(1L, updateRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Goal Type", result.getName());
        
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).existsByName("Updated Goal Type");
        verify(goalTypeRepository).save(any(GoalType.class));
    }
    
    @Test
    void update_shouldThrowException_whenNotFound() {
        // Given
        when(goalTypeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalTypeService.update(999L, updateRequest));
        assertEquals("Goal type not found with ID: 999", exception.getMessage());
        
        verify(goalTypeRepository).findById(999L);
        verify(goalTypeRepository, never()).save(any());
    }
    
    @Test
    void update_shouldThrowException_whenNameExists() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType1));
        when(goalTypeRepository.existsByName("Updated Goal Type")).thenReturn(true);
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalTypeService.update(1L, updateRequest));
        assertEquals("Goal type with name 'Updated Goal Type' already exists", exception.getMessage());
        
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).existsByName("Updated Goal Type");
        verify(goalTypeRepository, never()).save(any());
    }
    
    @Test
    void delete_shouldDeleteGoalType_whenNoGoals() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType1));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(0L);
        
        // When
        goalTypeService.delete(1L);
        
        // Then
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).countGoalsByTypeId(1L);
        verify(goalTypeRepository).delete(goalType1);
    }
    
    @Test
    void delete_shouldThrowException_whenNotFound() {
        // Given
        when(goalTypeRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> goalTypeService.delete(999L));
        assertEquals("Goal type not found with ID: 999", exception.getMessage());
        
        verify(goalTypeRepository).findById(999L);
        verify(goalTypeRepository, never()).delete(any());
    }
    
    @Test
    void delete_shouldThrowException_whenHasGoals() {
        // Given
        when(goalTypeRepository.findById(1L)).thenReturn(Optional.of(goalType1));
        when(goalTypeRepository.countGoalsByTypeId(1L)).thenReturn(5L);
        
        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> goalTypeService.delete(1L));
        assertEquals("Cannot delete goal type with ID 1 because it has 5 associated goals", exception.getMessage());
        
        verify(goalTypeRepository).findById(1L);
        verify(goalTypeRepository).countGoalsByTypeId(1L);
        verify(goalTypeRepository, never()).delete(any());
    }
} 