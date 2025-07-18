package io.dashboard.service;

import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalTargetResponse;
import io.dashboard.dto.IndicatorResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalTarget;
import io.dashboard.model.Indicator;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTargetRepository;
import io.dashboard.repository.IndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalTargetService {
    
    private final GoalTargetRepository goalTargetRepository;
    private final GoalRepository goalRepository;
    private final IndicatorRepository indicatorRepository;
    
    @Transactional(readOnly = true)
    public List<GoalTargetResponse> findByGoalId(Long goalId) {
        log.debug("Finding targets by goal ID: {}", goalId);
        List<GoalTarget> targets = goalTargetRepository.findByGoalIdWithDetails(goalId);
        return targets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GoalTargetResponse> findByIndicatorId(Long indicatorId) {
        log.debug("Finding targets by indicator ID: {}", indicatorId);
        List<GoalTarget> targets = goalTargetRepository.findByIndicatorIdWithDetails(indicatorId);
        return targets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public GoalTargetResponse create(GoalTarget target) {
        log.debug("Creating new goal target for goal ID: {} and indicator ID: {}", 
                target.getGoal().getId(), target.getIndicator().getId());
        
        // Validate goal exists
        Goal goal = goalRepository.findById(target.getGoal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", target.getGoal().getId()));
        
        // Validate indicator exists
        Indicator indicator = indicatorRepository.findById(target.getIndicator().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", target.getIndicator().getId()));
        
        target.setGoal(goal);
        target.setIndicator(indicator);
        
        // Validate target value
        validateTargetValue(target);
        
        GoalTarget savedTarget = goalTargetRepository.save(target);
        log.info("Created goal target with ID: {}", savedTarget.getId());
        return mapToResponse(savedTarget);
    }
    
    @Transactional
    public GoalTargetResponse update(Long id, GoalTarget target) {
        log.debug("Updating goal target with ID: {}", id);
        
        GoalTarget existingTarget = goalTargetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalTarget", "id", id));
        
        existingTarget.setTargetYear(target.getTargetYear());
        existingTarget.setTargetValue(target.getTargetValue());
        existingTarget.setTargetType(target.getTargetType());
        existingTarget.setTargetPercentage(target.getTargetPercentage());
        
        // Validate target value
        validateTargetValue(existingTarget);
        
        GoalTarget updatedTarget = goalTargetRepository.save(existingTarget);
        log.info("Updated goal target with ID: {}", id);
        return mapToResponse(updatedTarget);
    }
    
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting goal target with ID: {}", id);
        
        GoalTarget target = goalTargetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalTarget", "id", id));
        
        goalTargetRepository.delete(target);
        log.info("Deleted goal target with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public void validateTargetValue(GoalTarget target) {
        // Validate target year is reasonable (not in past, not too far in future)
        int currentYear = LocalDateTime.now().getYear();
        if (target.getTargetYear() < currentYear) {
            throw new BadRequestException("Target year cannot be in the past");
        }
        if (target.getTargetYear() > currentYear + 50) {
            throw new BadRequestException("Target year cannot be more than 50 years in the future");
        }
        
        // Validate target value is positive
        if (target.getTargetValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target value must be positive");
        }
        
        // Validate target percentage if provided
        if (target.getTargetPercentage() != null) {
            if (target.getTargetPercentage().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Target percentage cannot be negative");
            }
            if (target.getTargetPercentage().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new BadRequestException("Target percentage cannot exceed 100%");
            }
        }
    }
    
    private GoalTargetResponse mapToResponse(GoalTarget target) {
        GoalResponse goalResponse = GoalResponse.builder()
                .id(target.getGoal().getId())
                .name(target.getGoal().getName())
                .url(target.getGoal().getUrl())
                .year(target.getGoal().getYear())
                .description(target.getGoal().getDescription())
                .createdAt(target.getGoal().getCreatedAt())
                .targetCount(null) // Will be calculated separately if needed
                .build();
        
        IndicatorResponse indicatorResponse = new IndicatorResponse();
        indicatorResponse.setId(target.getIndicator().getId());
        indicatorResponse.setCode(target.getIndicator().getCode());
        indicatorResponse.setName(target.getIndicator().getName());
        indicatorResponse.setDescription(target.getIndicator().getDescription());
        indicatorResponse.setIsComposite(target.getIndicator().getIsComposite());
        indicatorResponse.setCreatedAt(target.getIndicator().getCreatedAt());
        
        return GoalTargetResponse.builder()
                .id(target.getId())
                .goal(goalResponse)
                .indicator(indicatorResponse)
                .targetYear(target.getTargetYear())
                .targetValue(target.getTargetValue())
                .targetType(target.getTargetType())
                .targetPercentage(target.getTargetPercentage())
                .createdAt(target.getCreatedAt())
                .build();
    }
} 