package io.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.GoalResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.GoalIndicator;
import io.dashboard.model.ImpactDirection;
import io.dashboard.repository.GoalGroupRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.dashboard.dto.GoalCreateRequest;
import io.dashboard.dto.GoalUpdateRequest;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private GoalGroupRepository goalGroupRepository;

    @Mock
    private GoalIndicatorRepository goalIndicatorRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private GoalService goalService;

    private GoalGroup testGoalGroup;
    private Goal testGoal;
    private GoalResponse expectedResponse;
    private ObjectMapper objectMapper;
    private JsonNode testAttributes;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        testAttributes = objectMapper.readTree("{\"key\": \"value\"}");
        
        testGoalGroup = GoalGroup.builder()
                .id(1L)
                .name("SDGs")
                .description("Sustainable Development Goals")
                .createdAt(LocalDateTime.now())
                .build();

        testGoal = Goal.builder()
                .id(1L)
                .goalGroup(testGoalGroup)
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes(testAttributes)
                .createdAt(LocalDateTime.now())
                .build();

        expectedResponse = GoalResponse.builder()
                .id(1L)
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes(testAttributes)
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
    void findByGoalGroupId_ShouldReturnGoalsForGroup() {
        // Given
        List<Goal> goals = Arrays.asList(testGoal);
        when(goalRepository.findByGoalGroupId(anyLong())).thenReturn(goals);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        List<GoalResponse> result = goalService.findByGoalGroupId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getName(), result.get(0).getName());
        verify(goalRepository).findByGoalGroupId(1L);
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
        GoalCreateRequest request = GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Goal 1")
                .url("https://example.com")
                .year(2025)
                .description("Test goal")
                .attributes(testAttributes)
                .build();

        when(goalGroupRepository.findById(1L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        GoalResponse result = goalService.create(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getName(), result.getName());
        verify(goalGroupRepository).findById(1L);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void create_ShouldThrowException_WhenGoalGroupNotFound() {
        // Given
        GoalCreateRequest request = GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Goal 1")
                .build();

        when(goalGroupRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.create(request));
        verify(goalGroupRepository).findById(1L);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateGoal_WhenValidData() throws Exception {
        // Given
        JsonNode updatedAttributes = objectMapper.readTree("{\"updated\": \"value\"}");
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .goalGroupId(2L)
                .type("quantitative")
                .name("Updated Goal")
                .url("https://updated.com")
                .year(2026)
                .description("Updated description")
                .attributes(updatedAttributes)
                .build();

        // Create an updated goal that reflects the changes
        Goal updatedGoal = Goal.builder()
                .id(1L)
                .goalGroup(testGoalGroup)
                .name("Updated Goal")
                .url("https://updated.com")
                .year(2026)
                .description("Updated description")
                .attributes(updatedAttributes)
                .createdAt(testGoal.getCreatedAt())
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalGroupRepository.findById(2L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(updatedGoal);
        when(goalRepository.countTargetsByGoalId(1L)).thenReturn(0L);

        // When
        GoalResponse result = goalService.update(1L, request);

        // Then
        assertNotNull(result);
        assertEquals("Updated Goal", result.getName());
        assertEquals("https://updated.com", result.getUrl());
        assertEquals(2026, result.getYear());
        assertEquals("Updated description", result.getDescription());
        verify(goalRepository).findById(1L);
        verify(goalGroupRepository).findById(2L);
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void update_ShouldThrowException_WhenGoalNotFound() {
        // Given
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Updated Goal")
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.update(1L, request));
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

    @Test
    void createGoal_WithIndicators_ShouldCreateGoalAndRelationships() {
        // Given
        when(goalGroupRepository.findById(1L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(false);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 2L)).thenReturn(false);

        // When
        GoalResponse result = goalService.create(GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Test Goal")
                .description("Test Goal Description")
                .url("http://test.com")
                .year(2024)
                .indicators(Arrays.asList(1L, 2L))
                .build());

        // Then
        assertNotNull(result);
        assertEquals("Goal 1", result.getName());
        verify(goalRepository).save(any(Goal.class));
        verify(goalIndicatorRepository, times(2)).save(any(GoalIndicator.class));
    }

    @Test
    void createGoal_WithNonExistentIndicators_ShouldSkipInvalidIndicators() {
        // Given
        when(goalGroupRepository.findById(1L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(indicatorRepository.existsById(1L)).thenReturn(false); // Non-existent indicator
        when(indicatorRepository.existsById(2L)).thenReturn(true);  // Valid indicator

        // When
        GoalResponse result = goalService.create(GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Test Goal")
                .description("Test Goal Description")
                .url("http://test.com")
                .year(2024)
                .indicators(Arrays.asList(1L, 2L))
                .build());

        // Then
        assertNotNull(result);
        verify(goalIndicatorRepository, times(1)).save(any(GoalIndicator.class)); // Only valid indicator
    }

    @Test
    void createGoal_WithExistingRelationships_ShouldSkipDuplicates() {
        // Given
        when(goalGroupRepository.findById(1L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(indicatorRepository.existsById(1L)).thenReturn(true);
        when(indicatorRepository.existsById(2L)).thenReturn(true);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 1L)).thenReturn(true); // Already exists
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 2L)).thenReturn(false);

        // When
        GoalResponse result = goalService.create(GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Test Goal")
                .description("Test Goal Description")
                .url("http://test.com")
                .year(2024)
                .indicators(Arrays.asList(1L, 2L))
                .build());

        // Then
        assertNotNull(result);
        verify(goalIndicatorRepository, times(1)).save(any(GoalIndicator.class)); // Only new relationship
    }

    @Test
    void createGoal_WithoutIndicators_ShouldCreateGoalOnly() {
        // Given
        GoalCreateRequest request = GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Test Goal")
                .description("Test Goal Description")
                .url("http://test.com")
                .year(2024)
                .build();

        when(goalGroupRepository.findById(1L)).thenReturn(Optional.of(testGoalGroup));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // When
        GoalResponse result = goalService.create(request);

        // Then
        assertNotNull(result);
        verify(goalRepository).save(any(Goal.class));
        verify(goalIndicatorRepository, never()).save(any(GoalIndicator.class));
    }

    @Test
    void updateGoal_WithIndicators_ShouldUpdateGoalAndReplaceRelationships() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        when(indicatorRepository.existsById(3L)).thenReturn(true);
        when(indicatorRepository.existsById(4L)).thenReturn(true);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 3L)).thenReturn(false);
        when(goalIndicatorRepository.existsByGoalIdAndIndicatorId(1L, 4L)).thenReturn(false);

        // When
        GoalResponse result = goalService.update(1L, GoalUpdateRequest.builder()
                .type("quantitative")
                .name("Updated Goal")
                .description("Updated Goal Description")
                .url("http://updatedtest.com")
                .year(2025)
                .indicators(Arrays.asList(3L, 4L))
                .build());

        // Then
        assertNotNull(result);
        verify(goalRepository).save(any(Goal.class));
        verify(goalIndicatorRepository).deleteByGoalId(1L);
        verify(goalIndicatorRepository, times(2)).save(any(GoalIndicator.class));
    }

    @Test
    void updateGoal_WithEmptyIndicators_ShouldRemoveAllRelationships() {
        // Given
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .type("quantitative")
                .name("Updated Goal")
                .description("Updated Goal Description")
                .url("http://updatedtest.com")
                .year(2025)
                .indicators(Arrays.asList())
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // When
        GoalResponse result = goalService.update(1L, request);

        // Then
        assertNotNull(result);
        verify(goalRepository).save(any(Goal.class));
        verify(goalIndicatorRepository).deleteByGoalId(1L);
        verify(goalIndicatorRepository, never()).save(any(GoalIndicator.class));
    }

    @Test
    void updateGoal_WithoutIndicators_ShouldNotModifyRelationships() {
        // Given
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .type("quantitative")
                .name("Updated Goal")
                .description("Updated Goal Description")
                .url("http://updatedtest.com")
                .year(2025)
                .indicators(null)
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);

        // When
        GoalResponse result = goalService.update(1L, request);

        // Then
        assertNotNull(result);
        verify(goalRepository).save(any(Goal.class));
        verify(goalIndicatorRepository, never()).deleteByGoalId(any());
        verify(goalIndicatorRepository, never()).save(any(GoalIndicator.class));
    }

    @Test
    void createGoal_WithInvalidGoalGroup_ShouldThrowException() {
        // Given
        when(goalGroupRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.create(GoalCreateRequest.builder()
                .goalGroupId(1L)
                .type("quantitative")
                .name("Test Goal")
                .description("Test Goal Description")
                .url("http://test.com")
                .year(2024)
                .indicators(Arrays.asList(1L, 2L))
                .build()));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    void updateGoal_WithInvalidGoal_ShouldThrowException() {
        // Given
        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.update(1L, GoalUpdateRequest.builder()
                .type("quantitative")
                .name("Updated Goal")
                .description("Updated Goal Description")
                .url("http://updatedtest.com")
                .year(2025)
                .indicators(Arrays.asList(3L, 4L))
                .build()));
        verify(goalRepository, never()).save(any(Goal.class));
    }

    @Test
    void updateGoal_WithInvalidGoalGroup_ShouldThrowException() {
        // Given
        GoalUpdateRequest request = GoalUpdateRequest.builder()
                .goalGroupId(999L)
                .type("quantitative")
                .name("Updated Goal")
                .description("Updated Goal Description")
                .url("http://updatedtest.com")
                .year(2025)
                .indicators(Arrays.asList(3L, 4L))
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(goalGroupRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalService.update(1L, request));
        verify(goalRepository, never()).save(any(Goal.class));
    }
} 