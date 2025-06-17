package io.dashboard.service;

import io.dashboard.dto.goal.*;
import io.dashboard.entity.Goal;
import io.dashboard.entity.GoalIndicator;
import io.dashboard.entity.GoalTarget;
import io.dashboard.enums.ImpactDirection;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Indicator;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.GoalTargetRepository;
import io.dashboard.repository.IndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoalIndicatorService {
    
    private final GoalIndicatorRepository goalIndicatorRepository;
    private final GoalRepository goalRepository;
    private final IndicatorRepository indicatorRepository;
    private final GoalTargetRepository goalTargetRepository;
    
    public GoalIndicatorResponse linkGoalToIndicator(Long goalId, Long indicatorId, BigDecimal weight, ImpactDirection impactDirection) {
        log.debug("Linking goal {} to indicator {} with weight {} and impact {}", goalId, indicatorId, weight, impactDirection);
        
        // Validate goal and indicator exist
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        
        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator not found with ID: " + indicatorId));
        
        // Check for existing relationship
        if (goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, indicatorId)) {
            throw new BadRequestException("Goal is already linked to this indicator");
        }
        
        // Validate weight
        validateWeight(weight);
        
        // Check total weight constraint (warning only)
        checkTotalWeightConstraint(goalId, weight);
        
        GoalIndicator goalIndicator = GoalIndicator.builder()
                .goal(goal)
                .indicator(indicator)
                .aggregationWeight(weight)
                .impactDirection(impactDirection)
                .build();
        
        GoalIndicator savedGoalIndicator = goalIndicatorRepository.save(goalIndicator);
        log.info("Linked goal {} to indicator {} with weight {}", goalId, indicatorId, weight);
        
        return mapToResponse(savedGoalIndicator);
    }
    
    public void unlinkGoalFromIndicator(Long goalId, Long indicatorId) {
        log.debug("Unlinking goal {} from indicator {}", goalId, indicatorId);
        
        GoalIndicator goalIndicator = goalIndicatorRepository.findByGoalIdAndIndicatorId(goalId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal-indicator relationship not found"));
        
        goalIndicatorRepository.delete(goalIndicator);
        log.info("Unlinked goal {} from indicator {}", goalId, indicatorId);
    }
    
    public GoalIndicatorResponse updateGoalIndicatorWeight(Long goalId, Long indicatorId, BigDecimal weight) {
        log.debug("Updating weight for goal {} and indicator {} to {}", goalId, indicatorId, weight);
        
        GoalIndicator goalIndicator = goalIndicatorRepository.findByGoalIdAndIndicatorId(goalId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal-indicator relationship not found"));
        
        validateWeight(weight);
        
        // Check total weight constraint (warning only)
        BigDecimal currentTotal = goalIndicatorRepository.sumWeightsByGoalId(goalId);
        BigDecimal newTotal = currentTotal.subtract(goalIndicator.getAggregationWeight()).add(weight);
        if (newTotal.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Total weight for goal {} will exceed 1.0: {}", goalId, newTotal);
        }
        
        goalIndicator.setAggregationWeight(weight);
        GoalIndicator updatedGoalIndicator = goalIndicatorRepository.save(goalIndicator);
        log.info("Updated weight for goal {} and indicator {} to {}", goalId, indicatorId, weight);
        
        return mapToResponse(updatedGoalIndicator);
    }
    
    public GoalIndicatorResponse updateImpactDirection(Long goalId, Long indicatorId, ImpactDirection impactDirection) {
        log.debug("Updating impact direction for goal {} and indicator {} to {}", goalId, indicatorId, impactDirection);
        
        GoalIndicator goalIndicator = goalIndicatorRepository.findByGoalIdAndIndicatorId(goalId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal-indicator relationship not found"));
        
        goalIndicator.setImpactDirection(impactDirection);
        GoalIndicator updatedGoalIndicator = goalIndicatorRepository.save(goalIndicator);
        log.info("Updated impact direction for goal {} and indicator {} to {}", goalId, indicatorId, impactDirection);
        
        return mapToResponse(updatedGoalIndicator);
    }
    
    public List<GoalIndicatorResponse> findIndicatorsByGoal(Long goalId) {
        log.debug("Finding indicators for goal: {}", goalId);
        
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal not found with ID: " + goalId);
        }
        
        return goalIndicatorRepository.findByGoalIdWithIndicator(goalId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<GoalIndicatorResponse> findGoalsByIndicator(Long indicatorId) {
        log.debug("Finding goals for indicator: {}", indicatorId);
        
        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator not found with ID: " + indicatorId);
        }
        
        return goalIndicatorRepository.findByIndicatorIdWithGoal(indicatorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public boolean validateGoalIndicatorLink(Long goalId, Long indicatorId) {
        return goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, indicatorId);
    }
    
    public GoalProgressResponse calculateGoalProgress(Long goalId) {
        log.debug("Calculating progress for goal: {}", goalId);
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        
        List<GoalIndicator> goalIndicators = goalIndicatorRepository.findByGoalIdWithIndicator(goalId);
        
        if (goalIndicators.isEmpty()) {
            return GoalProgressResponse.builder()
                    .goalId(goalId)
                    .goalName(goal.getName())
                    .overallProgress(BigDecimal.ZERO)
                    .indicatorProgress(new ArrayList<>())
                    .totalIndicators(0)
                    .indicatorsWithTargets(0)
                    .build();
        }
        
        List<IndicatorProgressInfo> indicatorProgressList = new ArrayList<>();
        BigDecimal totalWeightedProgress = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        int indicatorsWithTargets = 0;
        
        for (GoalIndicator goalIndicator : goalIndicators) {
            IndicatorProgressInfo progressInfo = calculateIndicatorProgress(goalIndicator);
            indicatorProgressList.add(progressInfo);
            
            if (progressInfo.getTargetValue() != null) {
                totalWeightedProgress = totalWeightedProgress.add(
                        progressInfo.getProgress().multiply(progressInfo.getWeight())
                );
                totalWeight = totalWeight.add(progressInfo.getWeight());
                indicatorsWithTargets++;
            }
        }
        
        BigDecimal overallProgress = totalWeight.compareTo(BigDecimal.ZERO) > 0 
                ? totalWeightedProgress.divide(totalWeight, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        return GoalProgressResponse.builder()
                .goalId(goalId)
                .goalName(goal.getName())
                .overallProgress(overallProgress)
                .indicatorProgress(indicatorProgressList)
                .totalIndicators(goalIndicators.size())
                .indicatorsWithTargets(indicatorsWithTargets)
                .build();
    }
    
    public List<GoalIndicatorResponse> bulkLinkIndicators(Long goalId, List<GoalIndicatorLinkRequest> requests) {
        log.debug("Bulk linking {} indicators to goal: {}", requests.size(), goalId);
        
        // Validate goal exists
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + goalId));
        
        // Validate all indicators exist and check for duplicates
        List<Long> indicatorIds = new ArrayList<>();
        for (GoalIndicatorLinkRequest request : requests) {
            if (!indicatorRepository.existsById(request.getIndicatorId())) {
                throw new ResourceNotFoundException("Indicator not found with ID: " + request.getIndicatorId());
            }
            if (goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, request.getIndicatorId())) {
                throw new BadRequestException("Goal is already linked to indicator: " + request.getIndicatorId());
            }
            if (indicatorIds.contains(request.getIndicatorId())) {
                throw new BadRequestException("Duplicate indicator ID in request: " + request.getIndicatorId());
            }
            indicatorIds.add(request.getIndicatorId());
        }
        
        // Validate weights
        BigDecimal totalWeight = requests.stream()
                .map(GoalIndicatorLinkRequest::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalWeight.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Total weight for goal {} will exceed 1.0: {}", goalId, totalWeight);
        }
        
        // Create all relationships
        List<GoalIndicatorResponse> responses = new ArrayList<>();
        for (GoalIndicatorLinkRequest request : requests) {
            Indicator indicator = indicatorRepository.findById(request.getIndicatorId()).orElseThrow();
            
            GoalIndicator goalIndicator = GoalIndicator.builder()
                    .goal(goal)
                    .indicator(indicator)
                    .aggregationWeight(request.getWeight())
                    .impactDirection(request.getImpactDirection())
                    .build();
            
            GoalIndicator savedGoalIndicator = goalIndicatorRepository.save(goalIndicator);
            responses.add(mapToResponse(savedGoalIndicator));
        }
        
        log.info("Bulk linked {} indicators to goal {}", requests.size(), goalId);
        return responses;
    }
    
    private void validateWeight(BigDecimal weight) {
        if (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(BigDecimal.ONE) > 0) {
            throw new BadRequestException("Weight must be between 0.0 and 1.0");
        }
    }
    
    private void checkTotalWeightConstraint(Long goalId, BigDecimal newWeight) {
        BigDecimal currentTotal = goalIndicatorRepository.sumWeightsByGoalId(goalId);
        BigDecimal newTotal = currentTotal.add(newWeight);
        if (newTotal.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Total weight for goal {} will exceed 1.0: {}", goalId, newTotal);
        }
    }
    
    private IndicatorProgressInfo calculateIndicatorProgress(GoalIndicator goalIndicator) {
        // Find the most recent target for this indicator and goal
        List<GoalTarget> targets = goalTargetRepository.findByGoalIdAndIndicatorId(
                goalIndicator.getGoal().getId(), 
                goalIndicator.getIndicator().getId()
        );
        
        BigDecimal currentValue = BigDecimal.ZERO; // This would come from actual data
        BigDecimal targetValue = null;
        BigDecimal progress = BigDecimal.ZERO;
        
        if (!targets.isEmpty()) {
            GoalTarget target = targets.get(0); // Use the first target for now
            targetValue = target.getTargetValue();
            
            if (targetValue.compareTo(BigDecimal.ZERO) > 0) {
                progress = currentValue.divide(targetValue, 4, RoundingMode.HALF_UP);
                
                // Apply impact direction
                if (goalIndicator.getImpactDirection() == ImpactDirection.NEGATIVE) {
                    progress = BigDecimal.ONE.subtract(progress);
                }
                
                // Ensure progress is between 0 and 1
                progress = progress.max(BigDecimal.ZERO).min(BigDecimal.ONE);
            }
        }
        
        return IndicatorProgressInfo.builder()
                .indicatorId(goalIndicator.getIndicator().getId())
                .indicatorName(goalIndicator.getIndicator().getName())
                .currentValue(currentValue)
                .targetValue(targetValue)
                .progress(progress)
                .weight(goalIndicator.getAggregationWeight())
                .impactDirection(goalIndicator.getImpactDirection())
                .unit(goalIndicator.getIndicator().getUnit() != null ? goalIndicator.getIndicator().getUnit().getCode() : null)
                .build();
    }
    
    private GoalIndicatorResponse mapToResponse(GoalIndicator goalIndicator) {
        return GoalIndicatorResponse.builder()
                .id(goalIndicator.getId())
                .goalId(goalIndicator.getGoal().getId())
                .goalName(goalIndicator.getGoal().getName())
                .indicatorId(goalIndicator.getIndicator().getId())
                .indicatorName(goalIndicator.getIndicator().getName())
                .aggregationWeight(goalIndicator.getAggregationWeight())
                .impactDirection(goalIndicator.getImpactDirection())
                .createdAt(goalIndicator.getCreatedAt())
                .updatedAt(goalIndicator.getUpdatedAt())
                .build();
    }
} 