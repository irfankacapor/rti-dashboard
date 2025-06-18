package io.dashboard.service;

import io.dashboard.dto.GoalTypeResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.GoalType;
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
public class GoalTypeService {
    
    private final GoalTypeRepository goalTypeRepository;
    
    @Transactional(readOnly = true)
    public List<GoalTypeResponse> findAll() {
        log.debug("Finding all goal types");
        List<GoalType> goalTypes = goalTypeRepository.findAllWithGoals();
        return goalTypes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public GoalTypeResponse findById(Long id) {
        log.debug("Finding goal type by ID: {}", id);
        GoalType goalType = goalTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalType", "id", id));
        return mapToResponse(goalType);
    }
    
    @Transactional
    public GoalTypeResponse create(GoalType goalType) {
        log.debug("Creating new goal type: {}", goalType.getName());
        
        // Validate unique name
        if (goalTypeRepository.existsByName(goalType.getName())) {
            throw new BadRequestException("Goal type with name '" + goalType.getName() + "' already exists");
        }
        
        GoalType savedGoalType = goalTypeRepository.save(goalType);
        log.info("Created goal type with ID: {}", savedGoalType.getId());
        return mapToResponse(savedGoalType);
    }
    
    @Transactional
    public GoalTypeResponse update(Long id, GoalType goalType) {
        log.debug("Updating goal type with ID: {}", id);
        
        GoalType existingGoalType = goalTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalType", "id", id));
        
        // Check if name is being changed and if it conflicts with existing name
        if (!existingGoalType.getName().equals(goalType.getName()) && 
            goalTypeRepository.existsByName(goalType.getName())) {
            throw new BadRequestException("Goal type with name '" + goalType.getName() + "' already exists");
        }
        
        existingGoalType.setName(goalType.getName());
        existingGoalType.setDescription(goalType.getDescription());
        
        GoalType updatedGoalType = goalTypeRepository.save(existingGoalType);
        log.info("Updated goal type with ID: {}", id);
        return mapToResponse(updatedGoalType);
    }
    
    @Transactional
    public void delete(Long id) {
        log.debug("Deleting goal type with ID: {}", id);
        
        GoalType goalType = goalTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoalType", "id", id));
        
        // Check if goal type has goals
        long goalCount = goalTypeRepository.countGoalsByGoalTypeId(id);
        if (goalCount > 0) {
            throw new BadRequestException("Cannot delete goal type with " + goalCount + " associated goals");
        }
        
        goalTypeRepository.delete(goalType);
        log.info("Deleted goal type with ID: {}", id);
    }
    
    private GoalTypeResponse mapToResponse(GoalType goalType) {
        long goalCount = goalTypeRepository.countGoalsByGoalTypeId(goalType.getId());
        return GoalTypeResponse.builder()
                .id(goalType.getId())
                .name(goalType.getName())
                .description(goalType.getDescription())
                .createdAt(goalType.getCreatedAt())
                .goalCount(goalCount)
                .build();
    }
} 