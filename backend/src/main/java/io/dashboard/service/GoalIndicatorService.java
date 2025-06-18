package io.dashboard.service;

import io.dashboard.dto.GoalIndicatorLinkRequest;
import io.dashboard.dto.GoalIndicatorResponse;
import io.dashboard.dto.GoalProgressResponse;
import io.dashboard.dto.IndicatorProgressItem;
import io.dashboard.exception.BadRequestException;
import io.dashboard.exception.ResourceNotFoundException;
import io.dashboard.model.Goal;
import io.dashboard.model.GoalIndicator;
import io.dashboard.model.ImpactDirection;
import io.dashboard.model.Indicator;
import io.dashboard.repository.GoalIndicatorRepository;
import io.dashboard.repository.GoalRepository;
import io.dashboard.repository.IndicatorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalIndicatorService {
    
    private final GoalIndicatorRepository goalIndicatorRepository;
    private final GoalRepository goalRepository;
    private final IndicatorRepository indicatorRepository;
    
    @Transactional
    public GoalIndicatorResponse linkGoalToIndicator(Long goalId, Long indicatorId, Double weight, ImpactDirection direction) {
        // Validate business rules first
        validateWeight(weight);
        
        // Then check entity existence
        validateGoalIndicatorLink(goalId, indicatorId);
        
        if (goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, indicatorId)) {
            throw new BadRequestException("Goal is already linked to this indicator");
        }
        
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        
        Indicator indicator = indicatorRepository.findById(indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Indicator", "id", indicatorId));
        
        GoalIndicator goalIndicator = new GoalIndicator();
        GoalIndicator.GoalIndicatorId id = new GoalIndicator.GoalIndicatorId();
        id.setGoalId(goalId);
        id.setIndicatorId(indicatorId);
        goalIndicator.setId(id);
        goalIndicator.setGoal(goal);
        goalIndicator.setIndicator(indicator);
        goalIndicator.setAggregationWeight(weight);
        goalIndicator.setImpactDirection(direction);
        
        GoalIndicator saved = goalIndicatorRepository.save(goalIndicator);
        return toResponse(saved);
    }
    
    @Transactional
    public void unlinkGoalFromIndicator(Long goalId, Long indicatorId) {
        if (!goalIndicatorRepository.existsByGoalIdAndIndicatorId(goalId, indicatorId)) {
            throw new ResourceNotFoundException("GoalIndicator", "goalId and indicatorId", 
                    goalId + " and " + indicatorId);
        }
        
        GoalIndicator.GoalIndicatorId id = new GoalIndicator.GoalIndicatorId();
        id.setGoalId(goalId);
        id.setIndicatorId(indicatorId);
        goalIndicatorRepository.deleteById(id);
    }
    
    @Transactional
    public GoalIndicatorResponse updateGoalIndicatorWeight(Long goalId, Long indicatorId, Double weight) {
        // Validate business rules first
        validateWeight(weight);
        
        GoalIndicator goalIndicator = goalIndicatorRepository.findByGoalIdAndIndicatorId(goalId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("GoalIndicator", "goalId and indicatorId", 
                        goalId + " and " + indicatorId));
        
        goalIndicator.setAggregationWeight(weight);
        GoalIndicator saved = goalIndicatorRepository.save(goalIndicator);
        return toResponse(saved);
    }
    
    @Transactional
    public GoalIndicatorResponse updateGoalIndicatorDirection(Long goalId, Long indicatorId, ImpactDirection direction) {
        GoalIndicator goalIndicator = goalIndicatorRepository.findByGoalIdAndIndicatorId(goalId, indicatorId)
                .orElseThrow(() -> new ResourceNotFoundException("GoalIndicator", "goalId and indicatorId", 
                        goalId + " and " + indicatorId));
        
        goalIndicator.setImpactDirection(direction);
        GoalIndicator saved = goalIndicatorRepository.save(goalIndicator);
        return toResponse(saved);
    }
    
    public List<GoalIndicatorResponse> findIndicatorsByGoal(Long goalId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        
        return goalIndicatorRepository.findByGoalId(goalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public List<GoalIndicatorResponse> findGoalsByIndicator(Long indicatorId) {
        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator", "id", indicatorId);
        }
        
        return goalIndicatorRepository.findByIndicatorId(indicatorId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public void validateGoalIndicatorLink(Long goalId, Long indicatorId) {
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        
        if (!indicatorRepository.existsById(indicatorId)) {
            throw new ResourceNotFoundException("Indicator", "id", indicatorId);
        }
    }
    
    public GoalProgressResponse calculateGoalProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", goalId));
        
        List<GoalIndicator> goalIndicators = goalIndicatorRepository.findByGoalId(goalId);
        
        if (goalIndicators.isEmpty()) {
            GoalProgressResponse response = new GoalProgressResponse();
            response.setGoalId(goalId);
            response.setGoalName(goal.getName());
            response.setOverallProgress(0.0);
            response.setTotalWeight(0.0);
            response.setProgressStatus("NO_INDICATORS");
            response.setIndicatorProgress(new ArrayList<>()); // Initialize empty list
            return response;
        }
        
        double totalWeight = goalIndicators.stream()
                .mapToDouble(GoalIndicator::getAggregationWeight)
                .sum();
        
        List<IndicatorProgressItem> indicatorProgress = goalIndicators.stream()
                .map(this::calculateIndicatorProgress)
                .collect(Collectors.toList());
        
        double overallProgress = calculateOverallProgress(goalIndicators, indicatorProgress);
        
        GoalProgressResponse response = new GoalProgressResponse();
        response.setGoalId(goalId);
        response.setGoalName(goal.getName());
        response.setOverallProgress(overallProgress);
        response.setIndicatorProgress(indicatorProgress);
        response.setTotalWeight(totalWeight);
        response.setProgressStatus(determineProgressStatus(overallProgress));
        
        return response;
    }
    
    @Transactional
    public List<GoalIndicatorResponse> bulkLinkIndicators(Long goalId, List<GoalIndicatorLinkRequest> links) {
        if (links == null || links.isEmpty()) {
            throw new BadRequestException("Links list cannot be empty");
        }
        
        if (!goalRepository.existsById(goalId)) {
            throw new ResourceNotFoundException("Goal", "id", goalId);
        }
        
        // Validate all requests first
        for (GoalIndicatorLinkRequest link : links) {
            validateWeight(link.getAggregationWeight());
        }
        
        return links.stream()
                .map(link -> linkGoalToIndicator(goalId, link.getIndicatorId(), link.getAggregationWeight(), link.getImpactDirection()))
                .collect(Collectors.toList());
    }
    
    private GoalIndicatorResponse toResponse(GoalIndicator goalIndicator) {
        GoalIndicatorResponse response = new GoalIndicatorResponse();
        response.setGoalId(goalIndicator.getGoal().getId());
        response.setGoalName(goalIndicator.getGoal().getName());
        response.setIndicatorId(goalIndicator.getIndicator().getId());
        response.setIndicatorName(goalIndicator.getIndicator().getName());
        response.setIndicatorCode(goalIndicator.getIndicator().getCode());
        response.setAggregationWeight(goalIndicator.getAggregationWeight());
        response.setImpactDirection(goalIndicator.getImpactDirection());
        response.setCreatedAt(goalIndicator.getCreatedAt());
        return response;
    }
    
    private IndicatorProgressItem calculateIndicatorProgress(GoalIndicator goalIndicator) {
        // This is a simplified calculation - in a real system, you would fetch actual indicator values
        // For now, we'll use mock data or calculate based on targets
        IndicatorProgressItem item = new IndicatorProgressItem();
        item.setIndicatorId(goalIndicator.getIndicator().getId());
        item.setIndicatorName(goalIndicator.getIndicator().getName());
        item.setIndicatorCode(goalIndicator.getIndicator().getCode());
        item.setWeight(goalIndicator.getAggregationWeight());
        item.setDirection(goalIndicator.getImpactDirection());
        
        // Mock values - in real implementation, fetch from FactIndicatorValue
        item.setCurrentValue(75.0); // Mock current value
        item.setTargetValue(100.0); // Mock target value
        item.setProgress(75.0); // Mock progress percentage
        item.setUnit(goalIndicator.getIndicator().getUnit() != null ? goalIndicator.getIndicator().getUnit().getCode() : null);
        
        return item;
    }
    
    private double calculateOverallProgress(List<GoalIndicator> goalIndicators, List<IndicatorProgressItem> indicatorProgress) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < goalIndicators.size(); i++) {
            GoalIndicator gi = goalIndicators.get(i);
            IndicatorProgressItem ip = indicatorProgress.get(i);
            
            double directionMultiplier = getDirectionMultiplier(gi.getImpactDirection());
            double contribution = ip.getProgress() * gi.getAggregationWeight() * directionMultiplier;
            
            weightedSum += contribution;
            totalWeight += gi.getAggregationWeight();
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
    
    private double getDirectionMultiplier(ImpactDirection direction) {
        return switch (direction) {
            case POSITIVE -> 1.0;
            case NEGATIVE -> -1.0;
            case NEUTRAL -> 0.0;
        };
    }
    
    private String determineProgressStatus(double progress) {
        if (progress >= 80.0) return "ON_TRACK";
        if (progress >= 60.0) return "AT_RISK";
        return "OFF_TRACK";
    }
    
    private void validateWeight(Double weight) {
        if (weight == null) {
            throw new BadRequestException("Aggregation weight is required");
        }
        if (weight < 0.0) {
            throw new BadRequestException("Aggregation weight must be between 0.0 and 1.0");
        }
        if (weight > 1.0) {
            throw new BadRequestException("Aggregation weight must be between 0.0 and 1.0");
        }
        if (weight == 0.0) {
            throw new BadRequestException("Aggregation weight must be greater than 0.0");
        }
    }
} 