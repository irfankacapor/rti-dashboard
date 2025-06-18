package io.dashboard.service;

import io.dashboard.dto.GoalSubareaLinkResponse;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalSubarea;
import io.dashboard.model.Subarea;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalSubareaRepository;
import io.dashboard.repository.SubareaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalSubareaService {
    
    private final GoalSubareaRepository goalSubareaRepository;
    private final GoalRepository goalRepository;
    private final SubareaRepository subareaRepository;
    
    @Transactional
    public GoalSubareaLinkResponse linkGoalToSubarea(Long goalId, Long subareaId) {
        validateGoalSubareaLink(goalId, subareaId);
        
        if (goalSubareaRepository.existsByGoalIdAndSubareaId(goalId, subareaId)) {
            throw new BadRequestException("Goal is already linked to this subarea");
        }
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        
        Subarea subarea = subareaRepository.findById(subareaId)
                .orElseThrow(() -> new ResourceNotFoundException("Subarea", "id", subareaId));
        
        GoalSubarea goalSubarea = new GoalSubarea();
        GoalSubarea.GoalSubareaId id = new GoalSubarea.GoalSubareaId();
        id.setGoalId(goalId);
        id.setSubareaId(subareaId);
        goalSubarea.setId(id);
        goalSubarea.setGoal(goal);
        goalSubarea.setSubarea(subarea);
        
        GoalSubarea saved = goalSubareaRepository.save(goalSubarea);
        return toResponse(saved);
    }
    
    @Transactional
    public void unlinkGoalFromSubarea(Long goalId, Long subareaId) {
        if (!goalSubareaRepository.existsByGoalIdAndSubareaId(goalId, subareaId)) {
            throw new ResourceNotFoundException("GoalSubarea", "goalId and subareaId", 
                    goalId + " and " + subareaId);
        }
        
        GoalSubarea.GoalSubareaId id = new GoalSubarea.GoalSubareaId();
        id.setGoalId(goalId);
        id.setSubareaId(subareaId);
        goalSubareaRepository.deleteById(id);
    }
    
    public List<GoalSubareaLinkResponse> findSubareasByGoal(Long goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        
        return goalSubareaRepository.findByGoalId(goalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<GoalSubareaLinkResponse> findGoalsBySubarea(Long subareaId) {
        if (!subareaRepository.existsById(subareaId)) {
            throw new ResourceNotFoundException("Subarea", "id", subareaId);
        }
        
        return goalSubareaRepository.findBySubareaId(subareaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public void validateGoalSubareaLink(Long goalId, Long subareaId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        
        if (!subareaRepository.existsById(subareaId)) {
            throw new ResourceNotFoundException("Subarea", "id", subareaId);
        }
    }
    
    private GoalSubareaLinkResponse toResponse(GoalSubarea goalSubarea) {
        GoalSubareaLinkResponse response = new GoalSubareaLinkResponse();
        response.setGoalId(goalSubarea.getGoal().getId());
        response.setGoalName(goalSubarea.getGoal().getName());
        response.setSubareaId(goalSubarea.getSubarea().getId());
        response.setSubareaName(goalSubarea.getSubarea().getName());
        response.setSubareaCode(goalSubarea.getSubarea().getCode());
        response.setCreatedAt(goalSubarea.getCreatedAt());
        return response;
    }
} 