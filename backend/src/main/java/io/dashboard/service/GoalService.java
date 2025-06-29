package io.dashboard.service;

import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalGroupResponse;
import io.dashboard.dto.GoalCreateRequest;
import io.dashboard.dto.GoalUpdateRequest;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalGroup;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalGroupRepository;
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
    
    @Transactional(readOnly = true)
    public Goal findGoalWithSubareas(Long goalId) {
        log.debug("Finding goal with subareas by ID: {}", goalId);
        Goal goal = goalRepository.findByIdWithSubareas(goalId);
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
        log.info("Created goal with ID: {}", savedGoal.getId());
        return mapToResponse(savedGoal);
    }
    
    @Transactional
    public GoalResponse update(Long id, GoalUpdateRequest request) {
        log.debug("Updating goal with ID: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        GoalGroup goalGroup = goalGroupRepository.findById(request.getGoalGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", request.getGoalGroupId()));
        goal.setGoalGroup(goalGroup);
        goal.setType(request.getType());
        goal.setName(request.getName());
        goal.setUrl(request.getUrl());
        goal.setYear(request.getYear());
        goal.setDescription(request.getDescription());
        goal.setAttributes(request.getAttributes());
        Goal updatedGoal = goalRepository.save(goal);
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
} 