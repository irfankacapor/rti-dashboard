package io.dashboard.service;

import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalGroupResponse;
import io.dashboard.dto.GoalCreateRequest;
import io.dashboard.dto.GoalUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalGroup;
import io.dashboard.model.GoalIndicator;
import io.dashboard.model.ImpactDirection;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalGroupRepository;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.IndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {
    
    private final GoalRepository goalRepository;
    private final GoalGroupRepository goalGroupRepository;
    private final GoalIndicatorRepository goalIndicatorRepository;
    private final IndicatorRepository indicatorRepository;
    
    @Transactional(readOnly = true)
    public List<GoalResponse> findAll() {
        log.debug("Finding all goals");
        List<Goal> goals = goalRepository.findAll();
        return goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GoalResponse> findByGoalGroupId(Long goalGroupId) {
        log.debug("Finding goals by goal group ID: {}", goalGroupId);
        List<Goal> goals = goalRepository.findByGoalGroupId(goalGroupId);
        return goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public GoalResponse findById(Long id) {
        log.debug("Finding goal by ID: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        return mapToResponse(goal);
    }
    
    @Transactional(readOnly = true)
    public Goal findGoalWithIndicators(Long goalId) {
        log.debug("Finding goal with indicators by ID: {}", goalId);
        Goal goal = goalRepository.findByIdWithIndicators(goalId);
        if (goal == null) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        return goal;
    }
    

    
    @Transactional
    public GoalResponse create(GoalCreateRequest request) {
        log.debug("Creating new goal: {}", request.getName());
        GoalGroup goalGroup = goalGroupRepository.findById(request.getGoalGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", request.getGoalGroupId()));
        Goal goal = Goal.builder()
                .goalGroup(goalGroup)
                .type(request.getType())
                .name(request.getName())
                .url(request.getUrl())
                .year(request.getYear())
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        Goal savedGoal = goalRepository.save(goal);
        
        // Handle indicator relationships if provided
        if (request.getIndicators() != null && !request.getIndicators().isEmpty()) {
            createGoalIndicatorRelationships(savedGoal.getId(), request.getIndicators());
        }
        
        log.info("Created goal with ID: {}", savedGoal.getId());
        return mapToResponse(savedGoal);
    }
    
    @Transactional
    public GoalResponse update(Long id, GoalUpdateRequest request) {
        log.debug("Updating goal with ID: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        
        // Only update goal group if provided
        if (request.getGoalGroupId() != null) {
            GoalGroup goalGroup = goalGroupRepository.findById(request.getGoalGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", request.getGoalGroupId()));
            goal.setGoalGroup(goalGroup);
        }
        
        goal.setType(request.getType());
        goal.setName(request.getName());
        goal.setUrl(request.getUrl());
        goal.setYear(request.getYear());
        goal.setDescription(request.getDescription());
        goal.setAttributes(request.getAttributes());
        Goal updatedGoal = goalRepository.save(goal);
        
        // Handle indicator relationships if provided
        if (request.getIndicators() != null) {
            // Remove existing relationships
            goalIndicatorRepository.deleteByGoalId(id);
            // Create new relationships
            if (!request.getIndicators().isEmpty()) {
                createGoalIndicatorRelationships(id, request.getIndicators());
            }
        }
        
        log.info("Updated goal with ID: {}", id);
        return mapToResponse(updatedGoal);
    }
    
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting goal with ID: {}", id);
        
        Goal goal = goalRepository.findByIdWithTargets(id);
        if (goal == null) {
            throw new ResourceNotFoundException("Goal", "id", id);
        }
        
        // Check if goal has targets
        long targetCount = goalRepository.countTargetsByGoalId(id);
        if (targetCount > 0) {
            throw new BadRequestException("Cannot delete goal with " + targetCount + " associated targets");
        }
        
        goalRepository.delete(goal);
        log.info("Deleted goal with ID: {}", id);
    }
    
    @Transactional(readOnly = true)
    public Goal findGoalsWithTargets(Long goalId) {
        log.debug("Finding goal with targets by ID: {}", goalId);
        Goal goal = goalRepository.findByIdWithTargets(goalId);
        if (goal == null) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        return goal;
    }
    
    private GoalResponse mapToResponse(Goal goal) {
        long targetCount = goalRepository.countTargetsByGoalId(goal.getId());
        GoalGroupResponse goalGroupResponse = GoalGroupResponse.builder()
                .id(goal.getGoalGroup().getId())
                .name(goal.getGoalGroup().getName())
                .description(goal.getGoalGroup().getDescription())
                .createdAt(goal.getGoalGroup().getCreatedAt())
                .goalCount(null)
                .build();
        return GoalResponse.builder()
                .id(goal.getId())
                .goalGroup(goalGroupResponse)
                .type(goal.getType())
                .name(goal.getName())
                .url(goal.getUrl())
                .year(goal.getYear())
                .description(goal.getDescription())
                .attributes(goal.getAttributes())
                .createdAt(goal.getCreatedAt())
                .targetCount(targetCount)
                .build();
    }
    
    private void createGoalIndicatorRelationships(Long goalId, List<Long> indicatorIds) {
        log.debug("Creating goal-indicator relationships for goal {} with {} indicators", goalId, indicatorIds.size());
        
        for (Long indicatorId : indicatorIds) {
            // Check if indicator exists
            if (!indicatorRepository.existsById(indicatorId)) {
                log.warn("Indicator with ID {} does not exist, skipping", indicatorId);
                continue;
            }
            
            // Check if relationship already exists
            if (goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, indicatorId)) {
                log.debug("Goal-indicator relationship already exists for goal {} and indicator {}", goalId, indicatorId);
                continue;
            }
            
            // Create the relationship
            GoalIndicator goalIndicator = new GoalIndicator();
            GoalIndicator.GoalIndicatorId id = new GoalIndicator.GoalIndicatorId();
            id.setGoalId(goalId);
            id.setIndicatorId(indicatorId);
            goalIndicator.setId(id);
            
            // Set the relationships properly - these are required for @MapsId to work
            Goal goal = goalRepository.findById(goalId).orElse(null);
            var indicator = indicatorRepository.findById(indicatorId).orElse(null);
            if (goal != null && indicator != null) {
                goalIndicator.setGoal(goal);
                goalIndicator.setIndicator(indicator);
            }
            
            // Set default values
            goalIndicator.setAggregationWeight(1.0); // Default weight
            goalIndicator.setImpactDirection(ImpactDirection.POSITIVE); // Default direction
            
            goalIndicatorRepository.save(goalIndicator);
            log.debug("Created goal-indicator relationship for goal {} and indicator {}", goalId, indicatorId);
        }
    }
} 