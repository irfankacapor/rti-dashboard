package io.dashboard.service;

import io.dashboard.dto.GoalResponse;
import io.dashboard.dto.GoalTypeResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalType;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTypeRepository;
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
    private final GoalTypeRepository goalTypeRepository;
    
    @Transactional(readOnly = true)
    public List<GoalResponse> findAll() {
        log.debug("Finding all goals");
        List<Goal> goals = goalRepository.findAll();
        return goals.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GoalResponse> findByGoalTypeId(Long goalTypeId) {
        log.debug("Finding goals by goal type ID: {}", goalTypeId);
        List<Goal> goals = goalRepository.findByGoalTypeIdWithTargets(goalTypeId);
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
    public GoalResponse create(Goal goal) {
        log.debug("Creating new goal: {}", goal.getName());
        
        // Validate goal type exists
        GoalType goalType = goalTypeRepository.findById(goal.getGoalType().getId())
                .orElseThrow(() -> new ResourceNotFoundException("GoalType", "id", goal.getGoalType().getId()));
        
        goal.setGoalType(goalType);
        Goal savedGoal = goalRepository.save(goal);
        log.info("Created goal with ID: {}", savedGoal.getId());
        return mapToResponse(savedGoal);
    }
    
    @Transactional
    public GoalResponse update(Long id, Goal goal) {
        log.debug("Updating goal with ID: {}", id);
        
        Goal existingGoal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        
        // Validate goal type exists if changed
        if (!existingGoal.getGoalType().getId().equals(goal.getGoalType().getId())) {
            GoalType goalType = goalTypeRepository.findById(goal.getGoalType().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("GoalType", "id", goal.getGoalType().getId()));
            existingGoal.setGoalType(goalType);
        }
        
        existingGoal.setName(goal.getName());
        existingGoal.setUrl(goal.getUrl());
        existingGoal.setYear(goal.getYear());
        existingGoal.setDescription(goal.getDescription());
        existingGoal.setAttributes(goal.getAttributes());
        
        Goal updatedGoal = goalRepository.save(existingGoal);
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
        
        GoalTypeResponse goalTypeResponse = GoalTypeResponse.builder()
                .id(goal.getGoalType().getId())
                .name(goal.getGoalType().getName())
                .description(goal.getGoalType().getDescription())
                .createdAt(goal.getGoalType().getCreatedAt())
                .goalCount(null) // Will be calculated separately if needed
                .build();
        
        return GoalResponse.builder()
                .id(goal.getId())
                .goalType(goalTypeResponse)
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