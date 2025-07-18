package io.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dashboard.dto.GoalTargetResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalTarget;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.Indicator;
import io.dashboard.model.TargetType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalTargetServiceTest {

    @Mock
    private GoalTargetRepository goalTargetRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private IndicatorRepository indicatorRepository;

    @InjectMocks
    private GoalTargetService goalTargetService;

    private GoalGroup testGoalGroup;
    private Goal testGoal;
    private Indicator testIndicator;
    private GoalTarget testTarget;
    private GoalTargetResponse expectedResponse;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();

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
                .createdAt(LocalDateTime.now())
                .build();

        testIndicator = Indicator.builder()
                .id(1L)
                .code("GDP")
                .name("Gross Domestic Product")
                .description("Economic indicator")
                .isComposite(false)
                .createdAt(LocalDateTime.now())
                .build();

        testTarget = GoalTarget.builder()
                .id(1L)
                .goal(testGoal)
                .indicator(testIndicator)
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .targetPercentage(new BigDecimal("10.5"))
                .createdAt(LocalDateTime.now())
                .build();

        expectedResponse = GoalTargetResponse.builder()
                .id(1L)
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .targetPercentage(new BigDecimal("10.5"))
                .createdAt(testTarget.getCreatedAt())
                .build();
    }

    @Test
    void findByGoalId_ShouldReturnTargetsForGoal() {
        // Given
        List<GoalTarget> targets = Arrays.asList(testTarget);
        when(goalTargetRepository.findByGoalIdWithDetails(1L)).thenReturn(targets);

        // When
        List<GoalTargetResponse> result = goalTargetService.findByGoalId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getTargetYear(), result.get(0).getTargetYear());
        verify(goalTargetRepository).findByGoalIdWithDetails(1L);
    }

    @Test
    void findByIndicatorId_ShouldReturnTargetsForIndicator() {
        // Given
        List<GoalTarget> targets = Arrays.asList(testTarget);
        when(goalTargetRepository.findByIndicatorIdWithDetails(1L)).thenReturn(targets);

        // When
        List<GoalTargetResponse> result = goalTargetService.findByIndicatorId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedResponse.getTargetYear(), result.get(0).getTargetYear());
        verify(goalTargetRepository).findByIndicatorIdWithDetails(1L);
    }

    @Test
    void create_ShouldCreateTarget_WhenValidData() {
        // Given
        GoalTarget targetToCreate = GoalTarget.builder()
                .goal(Goal.builder().id(1L).build())
                .indicator(Indicator.builder().id(1L).build())
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .targetPercentage(new BigDecimal("10.5"))
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));
        when(goalTargetRepository.save(any(GoalTarget.class))).thenReturn(testTarget);

        // When
        GoalTargetResponse result = goalTargetService.create(targetToCreate);

        // Then
        assertNotNull(result);
        assertEquals(expectedResponse.getTargetYear(), result.getTargetYear());
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalTargetRepository).save(any(GoalTarget.class));
    }

    @Test
    void create_ShouldThrowException_WhenGoalNotFound() {
        // Given
        GoalTarget targetToCreate = GoalTarget.builder()
                .goal(Goal.builder().id(1L).build())
                .indicator(Indicator.builder().id(1L).build())
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTargetService.create(targetToCreate));
        verify(goalRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenIndicatorNotFound() {
        // Given
        GoalTarget targetToCreate = GoalTarget.builder()
                .goal(Goal.builder().id(1L).build())
                .indicator(Indicator.builder().id(1L).build())
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTargetService.create(targetToCreate));
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenTargetYearInPast() {
        // Given
        GoalTarget targetToCreate = GoalTarget.builder()
                .goal(Goal.builder().id(1L).build())
                .indicator(Indicator.builder().id(1L).build())
                .targetYear(2020) // Past year
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.create(targetToCreate));
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowException_WhenTargetValueNegative() {
        // Given
        GoalTarget targetToCreate = GoalTarget.builder()
                .goal(Goal.builder().id(1L).build())
                .indicator(Indicator.builder().id(1L).build())
                .targetYear(2030)
                .targetValue(new BigDecimal("-1000000")) // Negative value
                .targetType(TargetType.ABSOLUTE)
                .build();

        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));
        when(indicatorRepository.findById(1L)).thenReturn(Optional.of(testIndicator));

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.create(targetToCreate));
        verify(goalRepository).findById(1L);
        verify(indicatorRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }

    @Test
    void update_ShouldUpdateTarget_WhenValidData() {
        // Given
        GoalTarget targetToUpdate = GoalTarget.builder()
                .targetYear(2035)
                .targetValue(new BigDecimal("2000000"))
                .targetType(TargetType.PERCENTAGE_CHANGE)
                .targetPercentage(new BigDecimal("20.0"))
                .build();

        when(goalTargetRepository.findById(1L)).thenReturn(Optional.of(testTarget));
        when(goalTargetRepository.save(any(GoalTarget.class))).thenReturn(testTarget);

        // When
        GoalTargetResponse result = goalTargetService.update(1L, targetToUpdate);

        // Then
        assertNotNull(result);
        verify(goalTargetRepository).findById(1L);
        verify(goalTargetRepository).save(any(GoalTarget.class));
    }

    @Test
    void update_ShouldThrowException_WhenTargetNotFound() {
        // Given
        GoalTarget targetToUpdate = GoalTarget.builder()
                .targetYear(2035)
                .targetValue(new BigDecimal("2000000"))
                .targetType(TargetType.PERCENTAGE_CHANGE)
                .build();

        when(goalTargetRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTargetService.update(1L, targetToUpdate));
        verify(goalTargetRepository).findById(1L);
        verify(goalTargetRepository, never()).save(any());
    }

    @Test
    void delete_ShouldDeleteTarget_WhenExists() {
        // Given
        when(goalTargetRepository.findById(1L)).thenReturn(Optional.of(testTarget));

        // When
        goalTargetService.delete(1L);

        // Then
        verify(goalTargetRepository).findById(1L);
        verify(goalTargetRepository).delete(testTarget);
    }

    @Test
    void delete_ShouldThrowException_WhenTargetNotFound() {
        // Given
        when(goalTargetRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> goalTargetService.delete(1L));
        verify(goalTargetRepository).findById(1L);
        verify(goalTargetRepository, never()).delete(any());
    }

    @Test
    void validateTargetValue_ShouldPass_WhenValidData() {
        // Given
        GoalTarget validTarget = GoalTarget.builder()
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .targetPercentage(new BigDecimal("10.5"))
                .build();

        // When & Then
        assertDoesNotThrow(() -> goalTargetService.validateTargetValue(validTarget));
    }

    @Test
    void validateTargetValue_ShouldThrowException_WhenTargetYearInPast() {
        // Given
        GoalTarget invalidTarget = GoalTarget.builder()
                .targetYear(2020) // Past year
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .build();

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.validateTargetValue(invalidTarget));
    }

    @Test
    void validateTargetValue_ShouldThrowException_WhenTargetYearTooFarInFuture() {
        // Given
        GoalTarget invalidTarget = GoalTarget.builder()
                .targetYear(2100) // Too far in future
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.ABSOLUTE)
                .build();

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.validateTargetValue(invalidTarget));
    }

    @Test
    void validateTargetValue_ShouldThrowException_WhenTargetValueNegative() {
        // Given
        GoalTarget invalidTarget = GoalTarget.builder()
                .targetYear(2030)
                .targetValue(new BigDecimal("-1000000")) // Negative value
                .targetType(TargetType.ABSOLUTE)
                .build();

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.validateTargetValue(invalidTarget));
    }

    @Test
    void validateTargetValue_ShouldThrowException_WhenTargetPercentageNegative() {
        // Given
        GoalTarget invalidTarget = GoalTarget.builder()
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.PERCENTAGE_CHANGE)
                .targetPercentage(new BigDecimal("-10.5")) // Negative percentage
                .build();

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.validateTargetValue(invalidTarget));
    }

    @Test
    void validateTargetValue_ShouldThrowException_WhenTargetPercentageExceeds100() {
        // Given
        GoalTarget invalidTarget = GoalTarget.builder()
                .targetYear(2030)
                .targetValue(new BigDecimal("1000000"))
                .targetType(TargetType.PERCENTAGE_CHANGE)
                .targetPercentage(new BigDecimal("150.0")) // Exceeds 100%
                .build();

        // When & Then
        assertThrows(BadRequestException.class, () -> goalTargetService.validateTargetValue(invalidTarget));
    }
} 