package io.dashboard.service;

import io.dashboard.dto.GoalSubareaLinkResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Area;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalSubarea;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.Subarea;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalSubareaRepository;
import io.dashboard.repository.SubareaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalSubareaServiceTest {

    @Mock
    private GoalSubareaRepository goalSubareaRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private SubareaRepository subareaRepository;

    @InjectMocks
    private GoalSubareaService goalSubareaService;

    private Goal goal;
    private Subarea subarea;
    private GoalGroup goalGroup;
    private Area area;

    @BeforeEach
    void setUp() {
        goalGroup = new GoalGroup();
        goalGroup.setId(1L);
        goalGroup.setName("SDGs");

        goal = new Goal();
        goal.setId(1L);
        goal.setName("Test Goal");
        goal.setGoalGroup(goalGroup);

        area = new Area();
        area.setId(1L);
        area.setName("Test Area");

        subarea = new Subarea();
        subarea.setId(1L);
        subarea.setName("Test Subarea");
        subarea.setCode("TEST_SUB");
        subarea.setArea(area);
    }

    @Test
    void linkGoalToSubarea_shouldSucceed() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(subareaRepository.findById(1L)).thenReturn(Optional.of(subarea));
        when(goalSubareaRepository.existsByGoalIdAndSubareaId(1L, 1L)).thenReturn(false);
        when(goalSubareaRepository.save(any(GoalSubarea.class))).thenAnswer(invocation -> {
            GoalSubarea gs = invocation.getArgument(0);
            gs.setCreatedAt(LocalDateTime.now());
            return gs;
        });

        // When
        GoalSubareaLinkResponse result = goalSubareaService.linkGoalToSubarea(1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getGoalId());
        assertEquals(1L, result.getSubareaId());
        assertEquals("Test Goal", result.getGoalName());
        assertEquals("Test Subarea", result.getSubareaName());
        verify(goalSubareaRepository).save(any(GoalSubarea.class));
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.linkGoalToSubarea(1L, 1L));
        verify(goalSubareaRepository, never()).save(any());
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenSubareaNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.existsById(1L)).thenReturn(false);
        // No need to stub findById for either

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.linkGoalToSubarea(1L, 1L));
        verify(goalSubareaRepository, never()).save(any());
    }

    @Test
    void linkGoalToSubarea_shouldFail_whenAlreadyLinked() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(goalSubareaRepository.existsByGoalIdAndSubareaId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThrows(BadRequestException.class, () ->
                goalSubareaService.linkGoalToSubarea(1L, 1L));
        verify(goalSubareaRepository, never()).save(any());
    }

    @Test
    void unlinkGoalFromSubarea_shouldSucceed() {
        // Given
        when(goalSubareaRepository.existsByGoalIdAndSubareaId(1L, 1L)).thenReturn(true);

        // When
        goalSubareaService.unlinkGoalFromSubarea(1L, 1L);

        // Then
        verify(goalSubareaRepository).deleteById(any(GoalSubarea.GoalSubareaId.class));
    }

    @Test
    void unlinkGoalFromSubarea_shouldFail_whenNotLinked() {
        // Given
        when(goalSubareaRepository.existsByGoalIdAndSubareaId(1L, 1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.unlinkGoalFromSubarea(1L, 1L));
        verify(goalSubareaRepository, never()).deleteById(any());
    }

    @Test
    void findSubareasByGoal_shouldReturnList() {
        // Given
        GoalSubarea gs1 = new GoalSubarea();
        gs1.setGoal(goal);
        gs1.setSubarea(subarea);

        when(goalRepository.existsById(1L)).thenReturn(true);
        when(goalSubareaRepository.findByGoalId(1L)).thenReturn(Arrays.asList(gs1));

        // When
        var result = goalSubareaService.findSubareasByGoal(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getSubareaId());
    }

    @Test
    void findSubareasByGoal_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.findSubareasByGoal(1L));
    }

    @Test
    void findGoalsBySubarea_shouldReturnList() {
        // Given
        GoalSubarea gs1 = new GoalSubarea();
        gs1.setGoal(goal);
        gs1.setSubarea(subarea);

        when(subareaRepository.existsById(1L)).thenReturn(true);
        when(goalSubareaRepository.findBySubareaId(1L)).thenReturn(Arrays.asList(gs1));

        // When
        var result = goalSubareaService.findGoalsBySubarea(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getGoalId());
        assertEquals(1L, result.get(0).getSubareaId());
    }

    @Test
    void findGoalsBySubarea_shouldFail_whenSubareaNotFound() {
        // Given
        when(subareaRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.findGoalsBySubarea(1L));
    }

    @Test
    void validateGoalSubareaLink_shouldSucceed_whenBothExist() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.existsById(1L)).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> goalSubareaService.validateGoalSubareaLink(1L, 1L));
    }

    @Test
    void validateGoalSubareaLink_shouldFail_whenGoalNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.validateGoalSubareaLink(1L, 1L));
    }

    @Test
    void validateGoalSubareaLink_shouldFail_whenSubareaNotFound() {
        // Given
        when(goalRepository.existsById(1L)).thenReturn(true);
        when(subareaRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                goalSubareaService.validateGoalSubareaLink(1L, 1L));
    }
} 