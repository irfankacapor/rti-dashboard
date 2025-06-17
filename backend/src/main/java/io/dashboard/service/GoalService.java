package io.dashboard.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoalService {
    
    private final GoalRepository goalRepository;
    private final GoalTypeRepository goalTypeRepository;
    private final GoalTargetRepository goalTargetRepository;
    private final GoalIndicatorRepository goalIndicatorRepository;
    private final IndicatorRepository indicatorRepository;
    private final ObjectMapper objectMapper;
    
    public List<GoalResponse> findAll() {
        log.debug("Finding all goals");
        return goalRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public GoalResponse findById(Long id) {
        log.debug("Finding goal by ID: {}", id);
        Goal goal = goalRepository.findByIdWithTargetsAndIndicators(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        return mapToResponse(goal);
    }
    
    public List<GoalResponse> findByType(Long goalTypeId) {
        log.debug("Finding goals by type ID: {}", goalTypeId);
        
        if (!goalTypeRepository.existsById(goalTypeId)) {
            throw new ResourceNotFoundException("Goal type not found with ID: " + goalTypeId);
        }
        
        return goalRepository.findByGoalTypeId(goalTypeId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public GoalResponse create(GoalCreateRequest request) {
        log.debug("Creating goal: {}", request.getName());
        
        GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal type not found with ID: " + request.getGoalTypeId()));
        
        validateAttributes(request.getAttributes());
        
        Goal goal = Goal.builder()
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .year(request.getYear())
                .goalType(goalType)
                .attributes(request.getAttributes())
                .build();
        
        Goal savedGoal = goalRepository.save(goal);
        log.info("Created goal with ID: {}", savedGoal.getId());
        
        return mapToResponse(savedGoal);
    }
    
    public GoalResponse update(Long id, GoalUpdateRequest request) {
        log.debug("Updating goal with ID: {}", id);
        
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        
        GoalType goalType = goalTypeRepository.findById(request.getGoalTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Goal type not found with ID: " + request.getGoalTypeId()));
        
        validateAttributes(request.getAttributes());
        
        goal.setName(request.getName());
        goal.setDescription(request.getDescription());
        goal.setUrl(request.getUrl());
        goal.setYear(request.getYear());
        goal.setGoalType(goalType);
        goal.setAttributes(request.getAttributes());
        
        Goal updatedGoal = goalRepository.save(goal);
        log.info("Updated goal with ID: {}", updatedGoal.getId());
        
        return mapToResponse(updatedGoal);
    }
    
    public void delete(Long id) {
        log.debug("Deleting goal with ID: {}", id);
        
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        
        long targetCount = goalRepository.countTargetsByGoalId(id);
        long indicatorCount = goalRepository.countIndicatorsByGoalId(id);
        
        if (targetCount > 0 || indicatorCount > 0) {
            throw new BadRequestException("Cannot delete goal with ID " + id + 
                    " because it has " + targetCount + " targets and " + indicatorCount + " indicator relationships");
        }
        
        goalRepository.delete(goal);
        log.info("Deleted goal with ID: {}", id);
    }
    
    public GoalTargetResponse addTarget(Long goalId, GoalTargetRequest request) {
        log.debug("Adding target to goal with ID: {}", goalId);
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        
        // Validate target values first before looking up indicator
        validateTargetYear(request.getTargetYear());
        validateTargetValue(request.getTargetValue());
        
        Indicator indicator = indicatorRepository.findById(request.getIndicatorId())
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found with ID: " + request.getIndicatorId()));
        
        GoalTarget target = GoalTarget.builder()
                .goal(goal)
                .indicator(indicator)
                .targetYear(request.getTargetYear())
                .targetValue(request.getTargetValue())
                .targetType(request.getTargetType())
                .targetPer(request.getTargetPer())
                .build();
        
        GoalTarget savedTarget = goalTargetRepository.save(target);
        log.info("Added target with ID: {} to goal with ID: {}", savedTarget.getId(), goalId);
        
        return mapToTargetResponse(savedTarget);
    }
    
    public void removeTarget(Long goalId, Long targetId) {
        log.debug("Removing target with ID: {} from goal with ID: {}", targetId, goalId);
        
        GoalTarget target = goalTargetRepository.findByGoalIdAndId(goalId, targetId)
                .orElseThrow(() -> new ResourceNotFoundException("Target not found with ID: " + targetId + " for goal with ID: " + goalId));
        
        goalTargetRepository.delete(target);
        log.info("Removed target with ID: {} from goal with ID: {}", targetId, goalId);
    }
    
    private void validateAttributes(String attributes) {
        if (attributes != null && !attributes.trim().isEmpty()) {
            try {
                objectMapper.readTree(attributes);
            } catch (Exception e) {
                throw new BadRequestException("Invalid JSON format in attributes: " + e.getMessage());
            }
        }
    }
    
    private void validateTargetYear(Integer targetYear) {
        if (targetYear < 1900 || targetYear > 2100) {
            throw new BadRequestException("Target year must be between 1900 and 2100");
        }
    }
    
    private void validateTargetValue(BigDecimal targetValue) {
        if (targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Target value must be positive");
        }
    }
    
    private GoalResponse mapToResponse(Goal goal) {
        List<GoalTargetResponse> targets = goalTargetRepository.findByGoalIdWithIndicator(goal.getId()).stream()
                .map(this::mapToTargetResponse)
                .collect(Collectors.toList());
        
        List<GoalIndicatorResponse> indicators = goalIndicatorRepository.findByGoalIdWithIndicator(goal.getId()).stream()
                .map(this::mapToIndicatorResponse)
                .collect(Collectors.toList());
        
        return GoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .description(goal.getDescription())
                .url(goal.getUrl())
                .year(goal.getYear())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .goalType(mapToGoalTypeResponse(goal.getGoalType()))
                .targets(targets)
                .indicators(indicators)
                .build();
    }
    
    private GoalTypeResponse mapToGoalTypeResponse(GoalType goalType) {
        long goalCount = goalTypeRepository.countGoalsByTypeId(goalType.getId());
        
        return GoalTypeResponse.builder()
                .id(goalType.getId())
                .name(goalType.getName())
                .description(goalType.getDescription())
                .createdAt(goalType.getCreatedAt())
                .updatedAt(goalType.getUpdatedAt())
                .goalCount(goalCount)
                .build();
    }
    
    private GoalTargetResponse mapToTargetResponse(GoalTarget target) {
        return GoalTargetResponse.builder()
                .id(target.getId())
                .indicatorId(target.getIndicator().getId())
                .indicatorName(target.getIndicator().getName())
                .targetYear(target.getTargetYear())
                .targetValue(target.getTargetValue())
                .targetType(target.getTargetType())
                .targetPer(target.getTargetPer())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .build();
    }
    
    private GoalIndicatorResponse mapToIndicatorResponse(GoalIndicator goalIndicator) {
        return GoalIndicatorResponse.builder()
                .id(goalIndicator.getId())
                .indicatorId(goalIndicator.getIndicator().getId())
                .indicatorName(goalIndicator.getIndicator().getName())
                .aggregationWeight(goalIndicator.getAggregationWeight())
                .impactDirection(goalIndicator.getImpactDirection())
                .createdAt(goalIndicator.getCreatedAt())
                .updatedAt(goalIndicator.getUpdatedAt())
                .build();
    }
} 