package io.dashboard.repository;

import io.dashboard.model.GoalTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalTargetRepository extends JpaRepository<GoalTarget, Long> {
    
    List<GoalTarget> findByGoalId(Long goalId);
    
    List<GoalTarget> findByIndicatorId(Long indicatorId);
    
    @Query("SELECT gt FROM GoalTarget gt LEFT JOIN FETCH gt.goal LEFT JOIN FETCH gt.indicator WHERE gt.goal.id = :goalId")
    List<GoalTarget> findByGoalIdWithDetails(Long goalId);
    
    @Query("SELECT gt FROM GoalTarget gt LEFT JOIN FETCH gt.goal LEFT JOIN FETCH gt.indicator WHERE gt.indicator.id = :indicatorId")
    List<GoalTarget> findByIndicatorIdWithDetails(Long indicatorId);
    
    @Query("SELECT gt FROM GoalTarget gt LEFT JOIN FETCH gt.goal LEFT JOIN FETCH gt.indicator WHERE gt.targetYear = :year")
    List<GoalTarget> findByTargetYearWithDetails(Integer year);
} 