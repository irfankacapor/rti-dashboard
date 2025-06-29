package io.dashboard.service;

import io.dashboard.dto.GoalGroupResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.GoalGroup;
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
public class GoalGroupService {
    
    private final GoalGroupRepository goalGroupRepository;
    
    @Transactional(readOnly = true)
    public List<GoalGroupResponse> findAll() {
        log.debug("Finding all goal groups");
        List<GoalGroup> goalGroups = goalGroupRepository.findAllWithGoals();
        return goalGroups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public GoalGroupResponse findById(Long id) {
        log.debug("Finding goal group by ID: {}", id);
        GoalGroup goalGroup = goalGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", id));
        return mapToResponse(goalGroup);
    }
    
    @Transactional
    public GoalGroupResponse create(GoalGroup goalGroup) {
        log.debug("Creating new goal group: {}", goalGroup.getName());
        
        // Validate unique name
        if (goalGroupRepository.existsByName(goalGroup.getName())) {
            throw new BadRequestException("Goal group with name '" + goalGroup.getName() + "' already exists");
        }
        
        GoalGroup savedGoalGroup = goalGroupRepository.save(goalGroup);
        log.info("Created goal group with ID: {}", savedGoalGroup.getId());
        return mapToResponse(savedGoalGroup);
    }
    
    @Transactional
    public GoalGroupResponse update(Long id, GoalGroup goalGroup) {
        log.debug("Updating goal group with ID: {}", id);
        
        GoalGroup existingGoalGroup = goalGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", id));
        
        // Check if name is being changed and if it conflicts with existing name
        if (!existingGoalGroup.getName().equals(goalGroup.getName()) && 
            goalGroupRepository.existsByName(goalGroup.getName())) {
            throw new BadRequestException("Goal group with name '" + goalGroup.getName() + "' already exists");
        }
        
        existingGoalGroup.setName(goalGroup.getName());
        existingGoalGroup.setDescription(goalGroup.getDescription());
        
        GoalGroup updatedGoalGroup = goalGroupRepository.save(existingGoalGroup);
        log.info("Updated goal group with ID: {}", id);
        return mapToResponse(updatedGoalGroup);
    }
    
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting goal group with ID: {}", id);
        
        GoalGroup goalGroup = goalGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalGroup", "id", id));
        
        // Check if goal group has goals
        long goalCount = goalGroupRepository.countGoalsByGoalGroupId(id);
        if (goalCount > 0) {
            throw new BadRequestException("Cannot delete goal group with " + goalCount + " associated goals");
        }
        
        goalGroupRepository.delete(goalGroup);
        log.info("Deleted goal group with ID: {}", id);
    }
    
    private GoalGroupResponse mapToResponse(GoalGroup goalGroup) {
        return GoalGroupResponse.builder()
                .id(goalGroup.getId())
                .name(goalGroup.getName())
                .description(goalGroup.getDescription())
                .createdAt(goalGroup.getCreatedAt())
                .goalCount((long) (goalGroup.getGoals() != null ? goalGroup.getGoals().size() : 0))
                .build();
    }
} 